package me.mucloud.application.mk.serverlauncher.mucore

import com.electronwill.nightconfig.core.file.FileConfig
import java.io.File
import java.nio.charset.StandardCharsets

class MuConfiguration{

    init{
        if(!getServerFolder().exists()) getServerFolder().mkdirs()
        if(!getLogFolder().exists()) getLogFolder().mkdirs()
    }

    private val configFile = File("config.yml")
    private val configBase: FileConfig = FileConfig.builder(configFile)
        .charset(StandardCharsets.UTF_8)
        .defaultResource(this.javaClass.protectionDomain.classLoader.getResource("config.yml")!!.path)
        .autoreload().sync()
        .build()

    var version: Int = configBase.getInt("ConfigVersion")
    var serverFolder: String = configBase.get("ServerFolderPath")
    var logFolder: String = configBase.get("LogFolderPath")
    var muCorePort: Int = configBase.getInt("MuCorePort")
    var muLinkPort: Int = configBase.getInt("muLinkPort")
    var systemMonitorInterval: Int = configBase.getInt("SystemMonitorInterval")

    fun getServerFolder(): File = File(serverFolder).absoluteFile
    fun getLogFolder(): File = File(logFolder).absoluteFile

    fun save(){
        configBase.save()
    }
}