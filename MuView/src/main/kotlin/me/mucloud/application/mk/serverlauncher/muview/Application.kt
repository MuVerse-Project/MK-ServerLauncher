package me.mucloud.application.mk.serverlauncher.muview

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironmentAdapter
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerAdapter
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerType
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerTypeSerializer
import me.mucloud.application.mk.serverlauncher.muview.mulink.initWebSocket
import me.mucloud.application.mk.serverlauncher.muview.view.initEnvRoute
import me.mucloud.application.mk.serverlauncher.muview.view.initServerRoute
import kotlin.time.Duration.Companion.seconds

val MuCore: MuCoreMini = MuCoreMini
lateinit var MuView: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(JavaEnvironment::class.java, JavaEnvironmentAdapter)
    .registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
    .registerTypeAdapter(MCJEServerType::class.java, MCJEServerTypeSerializer)
    .also { MuPacketFactory.addMuPacketAdapter(it) }
    .create()

var MuView_Port: Int = MuCore.getMuCoreConfig().getMuViewPort()
    private set

fun main() {
    MuCore.start()
    MuView = embeddedServer(Netty, port = MuView_Port, module = Application::module)
    MuView.addShutdownHook(MuView::stop)
    MuView.monitor.subscribe(ApplicationStopping) { MuCore.stop() }
    MuView.start(wait = true)
}

fun Application.installPlugins(){
    install(CORS){
        anyHost()
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
        allowHeader("Content-Type")
        allowSameOrigin = true
        allowNonSimpleContentTypes = true
    }
    install(ContentNegotiation){
        gson{
            setPrettyPrinting()
            registerTypeAdapter(JavaEnvironment::class.java, JavaEnvironmentAdapter)
            registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
            registerTypeAdapter(MCJEServerType::class.java, MCJEServerTypeSerializer)
                .also { MuPacketFactory.addMuPacketAdapter(it) }
        }
    }
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = GsonWebsocketContentConverter(gson)
    }
    install(Sessions){

    }
}

fun Application.initMuView(){
    routing {
        singlePageApplication {
            vue("MuView")
        }
    }
}

fun Application.module() {
    installPlugins()
    initServerRoute()
    initEnvRoute()
    initWebSocket()
    initMuView()
}
