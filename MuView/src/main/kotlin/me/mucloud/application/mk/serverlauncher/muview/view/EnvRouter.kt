package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment

fun Application.initEnvRoute(){
    routing {
        route("api/v1/env"){ // RESTful API | Environment
            get("list") {
                call.respond(EnvPool.getEnvList())
            }

            post("create"){
                val rawData = call.receive<JsonObject>()
                val envName = rawData["name"]?.asString ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing property \"Name\"")
                val envPath = rawData["path"]?.asString ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing property \"Path\"")
                EnvPool.regEnv(JavaEnvironment(envName, envPath))
                call.respond(HttpStatusCode.OK)
            }

            get("delete/{name}"){
                try{
                    if (EnvPool.delEnv(call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid Environment Name."))) call.respond(HttpStatusCode.OK, "Delete Success.") else call.respond(HttpStatusCode.InternalServerError, "MuCore Not have this Environment, please Re-Get ENVLIST.")
                }catch(e: NumberFormatException){
                    call.respond(HttpStatusCode.BadRequest, e.toString())
                }
            }
        }
    }
}