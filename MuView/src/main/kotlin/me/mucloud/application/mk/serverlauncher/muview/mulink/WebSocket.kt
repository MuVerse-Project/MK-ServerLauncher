package me.mucloud.application.mk.serverlauncher.muview.mulink

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import me.mucloud.application.mk.serverlauncher.mucore.external.SystemMonitor
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import me.mucloud.application.mk.serverlauncher.muview.gson
import kotlin.time.Duration.Companion.seconds

fun Application.initWebSocket() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = GsonWebsocketContentConverter(gson)
    }
    routing {
        // WebSocket >> Fetch System Status & AppInfo Pack & Server Status Info Flow
        webSocket("/overview") {
            log.info("Connection [System Monitor] -> [Status: CONNECTED]")
            SystemMonitor.getStatus().collect { s ->
                sendSerialized(s)
            }
        }

        // WebSocket >> Fetch Servers Info Flow
        webSocket("/server/{server}") {
            val server = call.parameters["server"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
            val target = ServerPool.getServer(server) ?: return@webSocket call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
            launch {
                incoming.consumeAsFlow().collect { raw ->
                    if (raw is Frame.Text) {
                        MuPacketFactory.toPacket(gson.toJsonTree(raw.readText()).asJsonObject).operate()
                    }
                }
                target.getServerFlow().collect {
                    sendSerialized(it)
                }
            }

        }
    }
}