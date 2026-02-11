package me.mucloud.application.mk.serverlauncher.muview

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironmentAdapter
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerAdapter
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerType
import me.mucloud.application.mk.serverlauncher.mucore.server.ServerTypeSerializer
import me.mucloud.application.mk.serverlauncher.muview.mulink.initSocket
import me.mucloud.application.mk.serverlauncher.muview.mulink.initWebSocket
import me.mucloud.application.mk.serverlauncher.muview.session.initMuSessionManager
import me.mucloud.application.mk.serverlauncher.muview.view.initRoute

var MuCore: MuCoreMini = MuCoreMini
    private set

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(JavaEnvironment::class.java, JavaEnvironmentAdapter)
    .registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
    .registerTypeAdapter(MCJEServerType::class.java, ServerTypeSerializer)
    .create()

fun main() {
    MuCore.start()
    embeddedServer(Netty, port = MuCore.getMuCoreConfig().muCorePort, module = Application::module).start(wait = true)
}

fun Application.module() {
    initMuSessionManager()
    initRoute()
    initWebSocket()
    initSocket()
    initMuView()
}

fun Application.initMuView(){
    routing {
        singlePageApplication {
//            useResources = false
//            filesPath = "MuView"
//            defaultPage = "index.html"
//            ignoreFiles {
//                it.endsWith(".txt")
//            }
            vue("MuView")
        }
    }
}
