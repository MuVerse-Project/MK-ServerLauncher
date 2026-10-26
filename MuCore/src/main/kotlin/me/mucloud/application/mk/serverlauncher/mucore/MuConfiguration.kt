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
     * 关闭接口的访问令牌。
     *
     * 留空表示**禁用** `POST /muclose`:部署在云服务器、经域名或端口转发访问时,
     * 来源地址既不可信也不稳定,靠回环判断无法区分"运维本人"和"路过的人",
     * 所以这里改成显式令牌,未配置就不开放。
     *
     * @since 0-beta1
     * @author Mu_Mu
     */
    private val MuCloseToken: String = instance.getOrElse("MuCloseToken", { "" })

    fun getServerFolder(): File = ServerFolder
    fun getLogFolder(): File = LogFolder
    fun getSystemMonitorInterval(): Duration = SystemMonitorInterval
    fun getMuCorePort(): Int = MuCorePort
    fun getMuViewPort(): Int = MuViewPort
    fun getMuCloseToken(): String = MuCloseToken

}