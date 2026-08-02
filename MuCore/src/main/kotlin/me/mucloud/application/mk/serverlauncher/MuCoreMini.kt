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
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muMsgErrPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muMsgInfoPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muMsgOKPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muMsgTipPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muMsgWarnPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.muEnvInfoPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.muEnvListPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.muServerInfoPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.muServerListPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.muServerLogPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.muServerStatusPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.muview.createMuServerPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.muview.deleteMuServerPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.muview.importMuServerPacketInfo
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
        regMuPackets()
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

    private fun regMuPackets(){
        MuPacketFactory.regMuPacket(muEnvInfoPacketInfo)
        MuPacketFactory.regMuPacket(muEnvListPacketInfo)
        MuPacketFactory.regMuPacket(muServerInfoPacketInfo)
        MuPacketFactory.regMuPacket(muServerListPacketInfo)
        MuPacketFactory.regMuPacket(muServerLogPacketInfo)
        MuPacketFactory.regMuPacket(muServerStatusPacketInfo)
        MuPacketFactory.regMuPacket(muMsgInfoPacketInfo)
        MuPacketFactory.regMuPacket(muMsgWarnPacketInfo)
        MuPacketFactory.regMuPacket(muMsgErrPacketInfo)
        MuPacketFactory.regMuPacket(muMsgOKPacketInfo)
        MuPacketFactory.regMuPacket(muMsgTipPacketInfo)
        MuPacketFactory.regMuPacket(createMuServerPacketInfo)
        MuPacketFactory.regMuPacket(deleteMuServerPacketInfo)
        MuPacketFactory.regMuPacket(importMuServerPacketInfo)
    }

}