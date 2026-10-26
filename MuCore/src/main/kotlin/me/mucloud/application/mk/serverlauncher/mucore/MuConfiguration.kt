package me.mucloud.application.mk.serverlauncher.mucore

import com.electronwill.nightconfig.core.file.FileConfig
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MuConfiguration{

    private val fileInstance = File("MK-ServerLauncher.yml")
    private val instance = FileConfig.builder(fileInstance)
        .autosave()
        .autoreload()
        .sync()
        .defaultResource("/MK-ServerLauncher.yml")
        .build()
        .also { it.load() }

    private var ServerFolder = File(instance.get<String>("ServerFolder"))
    private var LogFolder = File(instance.get<String>("LogFolder"))

    private var SystemMonitorInterval: Duration = instance.get<Long>("SystemMonitorInterval").seconds

    private val MuCorePort: Int = instance.get("MuCorePort")
    private val MuViewPort: Int = instance.get("MuViewPort")

    /**
     * MuView 的访问令牌(整站鉴权)。
     *
     * 留空表示**不启用鉴权**(仅建议本机开发时这样);对外部署请配置随机长串,
     * 打开页面时输入一次即可获得会话。
     *
     * @since VoidLand V0 | DEV.1
     * @author Mu_Mu
     */
    private val MuViewAccessToken: String = instance.getOrElse("MuViewAccessToken", { "" })

    fun getServerFolder(): File = ServerFolder
    fun getLogFolder(): File = LogFolder
    fun getSystemMonitorInterval(): Duration = SystemMonitorInterval
    fun getMuCorePort(): Int = MuCorePort
    fun getMuViewPort(): Int = MuViewPort
    fun getMuViewAccessToken(): String = MuViewAccessToken

}