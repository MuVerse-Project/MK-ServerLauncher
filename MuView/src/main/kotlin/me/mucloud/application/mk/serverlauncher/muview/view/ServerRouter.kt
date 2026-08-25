package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getValue
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.muserver.MuServerService
import me.mucloud.application.mk.serverlauncher.muview.MuView

fun Application.initServerRoute() {
    routing {
        get("muclose"){
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