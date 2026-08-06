package me.mucloud.application.mk.serverlauncher.muview.mulink

import io.ktor.http.*
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

fun Application.initWebSocket() {
    routing {
        // WebSocket >> Fetch System Status & AppInfo Pack & Server Status Info Flow
        webSocket("api/v1/overview") {
            log.info("Connection [System Monitor] -> [Status: CONNECTED]")
            SystemMonitor.getStatus().collect { s ->
                sendSerialized(s)
            }
        }

        // WebSocket >> Fetch Servers Info Flow
        webSocket("api/v1/server/{msid}") {
            val server = call.parameters["msid"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
            val callback = ServerPool.getMuServer(server)
            launch {
                incoming.consumeAsFlow().collect { raw ->
                    if (raw is Frame.Text) {
                        MuPacketFactory.toPacket(gson.toJsonTree(raw.readText()).asJsonObject)
                    }
                }
                if(callback.isOk){
                    callback.value!!.msec.collect {
                        sendSerialized(it)
                    }
                }else{
                    call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
                }
            }

        }
    }
}