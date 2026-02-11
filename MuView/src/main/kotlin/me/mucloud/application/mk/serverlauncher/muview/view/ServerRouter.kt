package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import java.io.File
import java.util.*
import java.util.jar.JarFile

fun Application.initServerRoute() {
    routing {
        route("api/v1/server") {
            val sp = ServerPool
            get("availableType") {
                call.respond(ServerPool.getAvailableTypes())
            }
            get("list") {
                call.respond(ServerPool.getServerList())
            }
            get("delete/{name}") {
                if (!sp.delServer(
                        call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Server Name Undefined"))
                    ) call.respond(HttpStatusCode.BadRequest, "Server Not Found") else call.respond(HttpStatusCode.OK)
            }
            get("remove/{name}") {
                ServerPool.removeServer(
                    call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                )
                call.respond(HttpStatusCode.OK)
            }
            get("start/{name}") {
                (ServerPool.getServer(
                    call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                ) ?: return@get call.respond(HttpStatusCode.BadRequest)).start()
                call.respond(HttpStatusCode.OK)
            }
            get("stop/{name}") {
                (ServerPool.getServer(
                    call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                ) ?: return@get call.respond(HttpStatusCode.BadRequest)).stop()
                call.respond(HttpStatusCode.OK)
            }

            post("create") {
                call.receive<JsonObject>().also { j ->
                    try {
                        MCJEServer(
                            j["name"].asString,
                            j["version"].asString,
                            ServerPool.getType(j["type"].asString),
                            j["desc"].asString,
                            j["port"].asInt,
                            EnvPool.getEnv(j["env"].asString)!!
                        )
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, e.toString())
                        e.printStackTrace()
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }

            post("import") {
                call.receive<JsonObject>().also { r ->
                    try {
                        val target = r["name"].asString
                        val targetPath = r["path"].asString

                        var type = "Unknown"
                        var version = "Unknown"
                        val targetServer = ServerPool.getServer(target)
                        if (targetServer != null || !File(targetPath).exists()) {
                            call.respond(HttpStatusCode.BadRequest)
                        } else {
                            val targetJar = JarFile(File(targetPath))
                            type = targetJar.manifest.mainAttributes["Main-Class"].toString().let {
                                if (it.contains("papermc")) {
                                    "paper"
                                } else if (it.contains("leavesmc")) {
                                    "leaves"
                                } else {
                                    "Unknown"
                                }
                            }
                            var versionFile = targetJar.getJarEntry("version.json")
                            if (versionFile != null) {
                                JsonParser.parseReader(
                                    targetJar.getInputStream(versionFile).bufferedReader()
                                ).asJsonObject.also { version = it["id"].asString }
                            } else {
                                versionFile = targetJar.getJarEntry("patch.properties")
                                if (versionFile != null) {
                                    Properties().also {
                                        it.load(targetJar.getInputStream(versionFile))
                                        version = it.getProperty("version")
                                    }
                                }
                            }
                        }

                        MCJEServer(
                            target, version, ServerPool.getType(type),
                            r["desc"].asString,
                            r["port"].asInt,
                            EnvPool.getEnv(r["env"].asString)!!
                        ).apply {
                            save()
                        }
                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, e.toString())
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}