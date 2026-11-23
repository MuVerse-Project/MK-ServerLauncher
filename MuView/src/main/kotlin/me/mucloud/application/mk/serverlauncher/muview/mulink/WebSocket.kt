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
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
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
            val server = call.parameters["msid"]
            if (server.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
                return@webSocket
            }
            val callback = ServerPool.getMuServer(server)
            if (!callback.isOk) {
                call.respond(HttpStatusCode.BadRequest, "Server Not Found.")
                return@webSocket
            }

            // 两条流必须并发跑：incoming.collect 会一直挂起等客户端消息，
            // 放在它后面的 msec.collect 永远执行不到（旧实现的推送全哑就是这个原因）。
            val serverInst = callback.value!!

            // 1) 服务端事件（状态 / 日志）推给前端
            launch {
                serverInst.msec.collect { sendSerialized(it) }
            }

            // 2) 客户端发来的包：同样走 MuPacketAPI 反序列化。
            //    MuPacketAPI4TS 与 MuPacketAPI4KT 同属 MuPacketAPI，前端发来的包也是
            //    MP_ID / MP_DATA / CID 的结构；可执行包就地 execute()，回包推回前端。
            launch {
                incoming.consumeAsFlow().collect { raw ->
                    if (raw !is Frame.Text) return@collect
                    val parsed = runCatching {
                        MuPacketFactory.toPacket(gson.toJsonTree(raw.readText()).asJsonObject)
                    }.getOrElse { e ->
                        log.warn("WebSocket [Server] cannot parse client packet: ${e.message}")
                        return@collect
                    }
                    if (parsed is ExecutableMuPacket) {
                        sendSerialized(parsed.execute())
                    }
                }
            }
        }
    }
}