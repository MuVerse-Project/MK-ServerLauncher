package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.muenv.MuEnvironmentService

fun Application.initEnvRoute(){
    routing {
        route("api/v1/env"){
            get("list") {
                call.respond(MuEnvironmentService.getMuEnvList())
            }

            post("create"){
                val rawData = call.receive<JsonObject>()
                val envName = rawData["name"]?.asString ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing property \"Name\"")
                val envPath = rawData["path"]?.asString ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing property \"Path\"")
                call.respond(MuEnvironmentService.regMuEnvironment(JavaEnvironment(envName, envPath)))
            }

            get("delete/{name}"){
                val evid: String by call.parameters
                call.respond(MuEnvironmentService.delMuEnvironment(evid))
            }
        }
    }
}