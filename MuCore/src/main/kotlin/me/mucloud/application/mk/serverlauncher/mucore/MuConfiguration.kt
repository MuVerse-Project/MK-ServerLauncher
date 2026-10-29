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
     * **留空 = 用户还没设置过令牌**。这种情况由服务端在启动时生成一次性临时令牌放在内存里
     * (不落盘),由 WebUI 首次登录页展示;用户设定自己的令牌后才写回本配置文件。
     *
     * @since VoidLand V0 | DEV.1
     * @author Mu_Mu
     */
    fun getMuViewAccessToken(): String = instance.get<String>("MuViewAccessToken") ?: ""

    /**
     * 写入新的访问令牌并立即落盘(FileConfig 已开启 autosave)。
     *
     * @since VoidLand V0 | DEV.1
     * @author Mu_Mu
     */
    fun setMuViewAccessToken(token: String) {
        instance.set<Any>("MuViewAccessToken", token)
        instance.save()
    }

    fun getServerFolder(): File = ServerFolder
    fun getLogFolder(): File = LogFolder
    fun getSystemMonitorInterval(): Duration = SystemMonitorInterval
    fun getMuCorePort(): Int = MuCorePort
    fun getMuViewPort(): Int = MuViewPort

}