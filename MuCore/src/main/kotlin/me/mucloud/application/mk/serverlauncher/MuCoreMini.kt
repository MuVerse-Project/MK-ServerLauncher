package me.mucloud.application.mk.serverlauncher

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.mucloud.application.mk.serverlauncher.mucore.MuConfiguration
import me.mucloud.application.mk.serverlauncher.mucore.external.AppInfoStatus
import me.mucloud.application.mk.serverlauncher.mucore.external.SystemMonitor
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironmentAdapter
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerAdapter
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerType
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServerTypeSerializer
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool

object MuCoreMini {

    private val MuCoreInfo: AppInfoStatus = AppInfoStatus("MuCore DEV Mini", "TinyNova V1 DEV.1")
    private val MuCoreConfiguration: MuConfiguration = MuConfiguration()
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(JavaEnvironment::class.java, JavaEnvironmentAdapter)
        .registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
        .registerTypeAdapter(MCJEServerType::class.java, MCJEServerTypeSerializer)
        .also { MuPacketFactory.addMuPacketAdapter(it) }
        .create()

    fun start() {
        EnvPool.scanEnv()
        ServerPool.scanMuServer()
        SystemMonitor.initMonitor(MuCoreConfiguration.getSystemMonitorInterval())
    }

    fun stop() {
        EnvPool.save()
        ServerPool.saveServers()
        SystemMonitor.close()
    }

    fun getMuCoreInfo(): AppInfoStatus = MuCoreInfo
    fun getMuCoreConfig(): MuConfiguration = MuCoreConfiguration

}