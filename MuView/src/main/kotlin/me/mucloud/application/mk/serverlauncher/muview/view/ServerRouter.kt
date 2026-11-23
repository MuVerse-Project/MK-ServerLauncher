package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getValue
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.muserver.MuServerService
import me.mucloud.application.mk.serverlauncher.muview.MuView

fun Application.initServerRoute() {
    routing {
        /**
         * 关闭 MuView。
         *
         * 只接受 POST:避免被链接预取、图片加载之类的方式误触发。
         * 准入由 [me.mucloud.application.mk.serverlauncher.muview.security.MuViewAccessGuard] 统一负责,
         * 这里不再单独判断来源地址或令牌。
         *
         * @since VoidLand V0 | DEV.1
         * @author Mu_Mu
         */
        post("muclose") {
            // 这条等于远程关服务:令牌一旦泄露就能被顺手关掉,所以只认本机来源。
            val from = call.request.local.remoteHost.trim().removePrefix("/").removeSurrounding("[", "]").lowercase()
            val loopback = from == "localhost" || from == "::1" || from == "0:0:0:0:0:0:0:1" ||
                from.startsWith("127.") || from.startsWith("::ffff:127.") ||
                runCatching { java.net.InetAddress.getByName(from).isLoopbackAddress }.getOrDefault(false)
            if (!loopback) {
                org.slf4j.LoggerFactory.getLogger("MuView")
                    .warn("[MuView] 拒绝远程关闭请求 来源={}", call.request.local.remoteHost)
                call.respond(
                    HttpStatusCode.Forbidden,
                    MuMsgErrPacket("MuView can only be closed from the local machine."),
                )
                return@post
            }
            call.respond(MuMsgOKPacket("MuView is shutting down."))
            MuView.stop()
 }
        route("api/v1/server") {
            get("availableType") {
                call.respond(MuServerService.getAvailableServerTypes())
            }
            get("list") {
                call.respond(MuServerService.getServerList())
            }
            get("delete/{msid}") {
                val msid: String by call.parameters
                call.respond(MuServerService.deleteMuServer(msid))
            }
            get("remove/{msid}") {
                val msid: String by call.parameters
                call.respond(MuServerService.removeMuServer(msid))
            }
            get("start/{msid}") {
                val msid: String by call.parameters
                call.respond(MuServerService.startMuServer(msid))
            }
            get("stop/{name}") {
                val msid: String by call.parameters
                call.respond(MuServerService.stopMuServer(msid, false))
            }
            get("forcestop/{name}"){
                val msid: String by call.parameters
                call.respond(MuServerService.stopMuServer(msid, true))
            }

            post("create") {
                val raw = call.receive<JsonObject>()
                val mp = MuPacketFactory.toPacket(raw)
                call.respond(if(mp is ExecutableMuPacket){ mp.execute() } else mp)
            }

            post("import") {
                val raw = call.receive<JsonObject>()
                val mp = MuPacketFactory.toPacket(raw)
                call.respond(if(mp is ExecutableMuPacket){ mp.execute() } else mp)
            }
        }
    }
}