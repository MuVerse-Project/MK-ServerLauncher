package me.mucloud.application.mk.serverlauncher.muview.view

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getValue
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.MuServerService
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import me.mucloud.application.mk.serverlauncher.muview.MuView
import java.io.File
import java.util.*
import java.util.jar.JarFile

fun Application.initServerRoute() {
    routing {
        get("muclose"){
            MuView.stop()
        }
        route("api/v1/server") {
            get("availableType") {
                call.respond(MuServerService.getAvaliableServerTypes())
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

            // TODO: Simply code
            post("create") {
                call.receive<JsonObject>().also { j ->
                    try {
                        val i = j["msi"].asJsonObject
                        val c = j["mssc"].asJsonObject
                        val msi = if(i.has("msl")) {
                            MCJEServer.Info(
                                msid = i["msid"].asString,
                                name = i["name"].asString,
                                version = i["version"].asString,
                                type = ServerPool.getType(i["type"].asString),
                                desc = i["desc"].asString,
                                env = EnvPool.getEnv(i["evid"].asString)
                                    .let { if(it.isOk) it.value!! else return@post call.respond(HttpStatusCode.BadRequest, "Env not found") },
                                port = i["port"].asInt,
                                msl = File(i["msl"].asString)
                            )
                        }else{
                            MCJEServer.Info(
                                msid = i["msid"].asString,
                                name = i["name"].asString,
                                version = i["version"].asString,
                                type = ServerPool.getType(i["type"].asString),
                                desc = i["desc"].asString,
                                env = EnvPool.getEnv(i["evid"].asString)
                                    .let { if(it.isOk) it.value!! else return@post call.respond(HttpStatusCode.BadRequest, "Env not found") },
                                port = i["port"].asInt
                            )
                        }

                        val mssc = MCJEServer.StartupConfig(
                            minMemory = c["min_mem"].asInt,
                            maxMemory = c["max_mem"].asInt,
                            hasGui = c["gui"].asBoolean,
                            jvmFlag = c["jvm_flag"].asString,
                        )
                        check(ServerPool.validate(msi).isOk){ "MuServer Info validation failed" }

                        val rawServer = MCJEServer(msi, mssc)
                        call.respond(MuServerService.createMuServer(rawServer))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.toString())
                        e.printStackTrace()
                    }
                }
            }

            //TODO: Simply code
            post("import") {
                call.receive<JsonObject>().also { r ->
                    try {
                        val i = r["msi"].asJsonObject
                        val c = r["mssc"].asJsonObject

                        val target = i["name"].asString
                        val targetPath = i["path"].asString

                        val type: String
                        val version: String
                        if (!File(targetPath).exists()) {
                            return@post call.respond(HttpStatusCode.BadRequest)
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
                                }else{
                                    version = "Unknown"
                                }
                            }
                        }

                        val msi = MCJEServer.Info(
                            ServerPool.randomMSID(),
                            target,
                            version,
                            ServerPool.getType(type),
                            i["desc"].asString,
                            EnvPool.getEnv(i["evid"].asString)
                                .let { if(it.isOk) it.value!! else return@post call.respond(HttpStatusCode.BadRequest, "Env not found") },
                            i["port"].asInt,
                            File(targetPath).parentFile
                        )

                        val mssc = MCJEServer.StartupConfig(
                            minMemory = c["min_mem"].asInt,
                            maxMemory = c["max_mem"].asInt,
                            hasGui = c["gui"].asBoolean,
                            jvmFlag = c["jvm_flag"].asString,
                        )

                        val rawServer = MCJEServer(msi, mssc)
                        call.respond(MuServerService.importMuServer(rawServer))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, e.toString())
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}