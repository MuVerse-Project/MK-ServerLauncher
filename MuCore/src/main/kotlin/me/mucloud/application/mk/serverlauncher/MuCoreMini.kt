package me.mucloud.application.mk.serverlauncher

import me.mucloud.application.mk.serverlauncher.mucore.MuConfiguration
import me.mucloud.application.mk.serverlauncher.mucore.external.AppInfoStatus
import me.mucloud.application.mk.serverlauncher.mucore.external.SystemMonitor
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool

object MuCoreMini {

    private val MuCoreInfo: AppInfoStatus = AppInfoStatus("MuCore DEV Mini", "TinyNova V1 DEV.1")
    private val MuCoreConfiguration: MuConfiguration = MuConfiguration()

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