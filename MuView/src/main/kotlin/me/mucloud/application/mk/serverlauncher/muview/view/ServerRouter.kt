package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getValue
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.muserver.MuServerService
import me.mucloud.application.mk.serverlauncher.muview.MuView
import java.security.MessageDigest

fun Application.initServerRoute() {
    routing {
        /**
         * 关闭 MuView。
         *
         * 只接受 POST,并且必须携带令牌:
         * 部署在云服务器上时,对外地址往往是域名 + 端口转发(甚至一层反代),
         * 请求来源既不可信也不固定 —— 回环判断在这种情况下没有意义,
         * 反代转发过来的请求反而会被当成"本机"放行。所以这里以显式令牌为准,
         * 未配置 `MuCloseToken` 时接口直接关闭。
         *
         * @since 0-beta1
         * @author Mu_Mu
         */
        post("muclose") {
            val expected = MuCoreMini.getMuCoreConfig().getMuCloseToken()
            if (expected.isBlank()) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    MuMsgErrPacket("MuClose is disabled: set MuCloseToken in MK-ServerLauncher.yml to enable it.")
                )
                return@post
            }

            val provided = call.request.headers["X-MuClose-Token"]
                ?: call.request.queryParameters["token"]
            if (provided == null || !MessageDigest.isEqual(
                    provided.toByteArray(Charsets.UTF_8),
                    expected.toByteArray(Charsets.UTF_8)
                )
            ) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    MuMsgErrPacket("Invalid MuClose token.")
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