package me.mucloud.application.mk.serverlauncher.mucore

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MuConfiguration{

    private val instance = CommentedFileConfig.builder("MK-ServerLauncher.yaml")
        .autosave()
        .autoreload()
        .sync()
        .defaultResource("MK-ServerLauncher.yaml")
        .build()

    private var ServerFolder = File(instance.get<String>("ServerFolder"))
    private var LogFolder = File(instance.get<String>("LogFolder"))

    private var SystemMonitorInterval: Duration = instance.get<Long>("SystemMonitorInterval").seconds

    private val MuCorePort: Int = instance.get("MuCorePort")
    private val MuViewPort: Int = instance.get("MuViewPort")

    fun getServerFolder(): File = ServerFolder
    fun getLogFolder(): File = LogFolder
    fun getSystemMonitorInterval(): Duration = SystemMonitorInterval
    fun getMuCorePort(): Int = MuCorePort
    fun getMuViewPort(): Int = MuViewPort

}