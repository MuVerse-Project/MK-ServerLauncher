package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import me.mucloud.application.mk.serverlauncher.muenv.MuEnvironmentService
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory

fun Application.initEnvRoute(){
    routing {
        route("api/v1/env"){
            get("list") {
                call.respond(MuEnvironmentService.getMuEnvList())
            }

            post("create"){
                val rawData = call.receive<JsonObject>()
                val mp = MuPacketFactory.toPacket(rawData)
                call.respond(if(mp is ExecutableMuPacket){ mp.execute() } else mp)
            }

            get("delete/{name}"){
                val name: String by call.parameters
                call.respond(MuEnvironmentService.delMuEnvironment(name))
            }
        }
    }
}