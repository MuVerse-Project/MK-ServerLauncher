package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.file.FileConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket2View
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerLogPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerStatusPacket
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool.randomMSID
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

private const val LOG_PREFIX: String = "MuServer"

/**
 * 启动完成信号：`Done (3.214s)! For help, type "help"`
 *
 * 这是 MC 服务端在 stdout 上给出的**唯一确定判据**，收到它才认为真的起来了。
 */
private val STARTED_RE = Regex("""Done \(\d+(?:\.\d+)?s\)!""")

/**
 * 启动即失败的关键字：看到就直接进 ERROR，不用等进程退出。
 */
private val FAIL_RE = Regex(
    """(You need to agree to the EULA|Failed to start the minecraft server|Address already in use)"""
)

/** MuServer 事件流的缓冲深度；没有订阅者时按 DROP_OLDEST 丢弃，不阻塞发送方。 */
private const val EVENT_BUFFER: Int = 256

/** 收流用的调度器：都是阻塞 IO，单独开池，不占 Dispatchers.IO 的共享配额。 */
private object Dispat {
    val IO = java.util.concurrent.Executors.newCachedThreadPool { r ->
        Thread(r, "MuServer-IO").apply { isDaemon = true }
    }.asCoroutineDispatcher()
}

/**
 * # | MuExtension - MCJEServer
 * ## MC Java Edition Server
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Cloud
 */
class MCJEServer(
    // MuServer Basic Info
    val msi: Info,

    // MuServer Startup Config
    val mssc: StartupConfig,
){
    // MuServer Core File
    private val instance: File = msi.msl.resolve("core.jar")

    // MuServer Status
    var mss: ServerStatus by Delegates.observable(ServerStatus.CREATING){ _, prev, current ->
        when(prev){
            ServerStatus.CREATING -> info(LOG_PREFIX, "Deploy ${msi.msid} Server Successfully.")
            ServerStatus.ERROR -> info(LOG_PREFIX, "${msi.msid} has been unlock and change to STOPPED Status. Please check the errors when running MuServer.")
            ServerStatus.RESTARTING -> info(LOG_PREFIX, "${msi.msid} has been restarted.")
            else -> info(LOG_PREFIX, "${msi.msid} Status changed from $prev to $current")
        }
        CoroutineScope(Dispatchers.IO).launch { sendToView(MuServerStatusPacket(this@MCJEServer, current)) }
    }; private set

    // MuServer Process Lock
    var mspl: MuServerProcessLck? = null
        private set

    // MuTasks
    val muTaskPool: MutableList<String> = mutableListOf()

    // MuServer Event Channel
    // 带缓冲：没有订阅者时 tryEmit 直接丢弃最旧的，不会像无缓冲时那样把 emit 挂住
    val msec: MutableSharedFlow<MuPacket2View> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * 收流与状态转移都在这个作用域里跑。
     * SupervisorJob：一条流崩了不影响另一条；terminate 时整体 cancel。
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispat.IO)

    /**
     *  start() 递增。回调里拿到的 gen 与它不符就说明是旧进程的迟到事件，直接丢弃。
     */
    @Volatile
    private var generation: AtomicInteger = AtomicInteger(0)

    /**
     * 常驻的 stdin 写入端。只在进程退出或 terminate 时关闭，
     * 避免旧实现里每条命令都 new 一个 PrintWriter + use{} 把 stdin 关掉。
     */
    @Volatile
    private var stdinWriter: PrintWriter? = null

    // Server Process
    private lateinit var msp: Process

    // Server Configuration
    val msc: Configuration = Configuration()

    /**
     * 部署：铺 core.jar、同意 EULA、载入配置。
     *
     * 原实现在这里 `startMuServer(); stopMuServer(enforce = true)` —— 靠「起一次再杀掉」来生成
     * EULA 与 server.properties，但会一直卡在启动流程里（runProcess 阻塞），而且把状态搅乱。
     * 现在改成**只铺文件，不启动**；EULA 直接写死，配置由 [Configuration.tryLoad] 兜底生成。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun deploy() {
        info(LOG_PREFIX, "MuServer ${msi.msid} start deploying...")

        val rawCore = msi.type.getCoreFile(msi.version)
        rawCore.copyTo(instance, overwrite = true)

        msi.msl.resolve("eula.txt").writeText(
            """
            #By changing the setting below to TRUE you are indicating your agreement to our EULA.
            eula=true
            """.trimIndent() + System.lineSeparator()
        )

        msc.tryLoad()
    }

    fun runMuTasks(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            muTaskPool.forEach { tsk ->
                val proc = ProcessBuilder(tsk).start()
                proc.errorStream.bufferedReader().use {
                    info("MuServer-${msi.msid}-MuTask", "Running MuTask.")
                    sendToView(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, it.readText()))
                }
                if (proc.waitFor() == 0) {
                    sendToView(
                        MuServerLogPacket(
                            this@MCJEServer,
                            MuServerLogPacket.LogLevel.INFO,
                            "MuServer-${msi.msid}-MuTask finished"
                        )
                    )
                } else {
                    sendToView(
                        MuServerLogPacket(
                            this@MCJEServer,
                            MuServerLogPacket.LogLevel.ERROR,
                            "MuServer-${msi.msid}-MuTask encountered an unexpected error!"
                        )
                    )
                }
            }
        }
    }

    /**
     * 启动服务器。
     *
     * 状态流：STOPPED → PREPARING（跑 MuTasks）→ STARTING（进程已起、等 Done）→ RUNNING。
     * RUNNING 只由 stdout 收到 `Done (x.xs)!` 时置位，不在本方法里猜。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun startMuServer() {
        if (mss == ServerStatus.ERROR) {
            transition(ServerStatus.STOPPED, generation.get())
        }
        if (mss != ServerStatus.STOPPED) return

        val gen = generation.incrementAndGet()
        transition(ServerStatus.PREPARING, gen)
        runMuTasks().get()
        if (gen != generation.get()) return

        transition(ServerStatus.STARTING, gen)
        runProcess(gen)
    }

    /**
     * 停止服务器。
     *
     * 只负责把状态推到 STOPPING 并发出 stop；最终 STOPPED 由 onProcessExit 收尾。
     * 不做超时强杀 —— 由用户从前端「强制关闭」按钮触发 [enforce]。
     *
     * @param enforce 直接 destroyForcibly，对应 /api/v1/server/forcestop/{msid}
     * @since VoidLand V0 DEV.1
     */
    fun stopMuServer(enforce: Boolean = false) {
        if (mss != ServerStatus.STARTING && mss != ServerStatus.RUNNING) return

        val gen = generation.get()
        transition(ServerStatus.STOPPING, gen)
        if (enforce) {
            runCatching { msp.destroyForcibly() }
        } else {
            sendMessage("Server Stopping.")
            sendCommand("stop")
        }
    }

    /**
     * 重启：等旧进程真正退出后再起新的。
     *
     * 旧实现的 `msp.destroy(); runProcess()` 会在旧进程还没退时把新进程拉起来，撞端口。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun restartMuServer() {
        if (mss != ServerStatus.RUNNING) return

        val gen = generation.get()
        transition(ServerStatus.RESTARTING, gen)
        sendMessage("Server Restarting.")

        scope.launch {
            runCatching { msp.onExit().get() }
            if (gen != generation.get()) return@launch
            // 旧进程已退出，走一次完整启动（新 generation）
            transition(ServerStatus.STOPPED, gen)
            startMuServer()
        }
    }

    fun sendMessage(msg: String) {
        sendCommand("say [MKSL] $msg")
    }

    /**
     * 向服务端 stdin 发命令。
     *
     * 写入端是常驻的 [stdinWriter]，只在进程退出时关闭；
     * 旧实现每条命令都 `PrintWriter(...).use{}`，第一条之后 stdin 就废了。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun sendCommand(cmd: String) {
        val writer = stdinWriter ?: return
        if (mss != ServerStatus.RUNNING && mss != ServerStatus.STOPPING) return
        runCatching {
            writer.println(cmd)
            writer.flush()
        }
    }

    fun sendToView(packet: MuPacket2View){
        CoroutineScope(Dispatchers.IO).launch {
            msec.emit(packet)
        }
    }

    /**
     * 起进程，并把 stdout / stderr 交给收流协程。
     *
     * 与原实现的区别：
     *  1. 不再 redirectOutput(file) —— stdout 也接回手上，才能匹配启动完成信号
     *  2. 不再 readText() 阻塞 —— 两个流各自在协程里逐行读
     *  3. stdinWriter 提成字段，整条生命周期只关闭一次
     *
     * @since VoidLand V0 | DEV.1
     */
    private fun runProcess(gen: Int) {
        val hJvmargs: List<String> = buildList {
            add("-Xms${mssc.minMemory}M")
            add("-Xmx${mssc.maxMemory}M")
            if (mssc.jvmFlag.isNotEmpty()) {
                mssc.jvmFlag.split(" ").filter { it.isNotEmpty() }.forEach { add(it) }
            }
            if (!mssc.hasGui) add("--nogui")
        }

        msp = ProcessBuilder(
            msi.env.getAbsoluteExecPath(), "-jar", *hJvmargs.toTypedArray(), instance.absolutePath
        )
            .directory(msi.msl)
            .redirectErrorStream(false)
            .start()

        mspl = MuServerProcessLck(msp.pid(), LocalDateTime.now())
        generateMuServerLock()

        stdinWriter = PrintWriter(msp.outputStream, true)

        collectStream(msp.inputStream, gen)
        collectStream(msp.errorStream, gen)

        msp.onExit().thenAccept { p -> onProcessExit(p, gen) }
    }

    /**
     * 逐行收流：写日志文件 + 推给前端 + 判断状态。
     *
     * stdout 是启动完成信号（`Done (3.214s)!`）的来源；stderr 只做日志与失败兜底。
     *
     * @since VoidLand V0 | DEV.1
     */
    private fun collectStream(stream: java.io.InputStream, gen: Int) {
        scope.launch {
            val logFile = msi.msl.resolve("mksl-${msi.msid}.log")
            stream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (gen != generation.get()) return@useLines
                    runCatching { logFile.appendText(line + System.lineSeparator()) }
                    sendToView(
                        MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, line)
                    )
                    if (STARTED_RE.containsMatchIn(line)) {
                        transition(ServerStatus.RUNNING, gen)
                    } else if (FAIL_RE.containsMatchIn(line)) {
                        transition(ServerStatus.ERROR, gen)
                    }
                }
            }
        }
    }

    /**
     * 进程退出：唯一能把状态推到 STOPPED / ERROR 的地方。
     *
     * @since VoidLand V0 | DEV.1
     */
    private fun onProcessExit(p: Process, gen: Int) {
        runCatching { stdinWriter?.close() }
        stdinWriter = null
        if (gen != generation.get()) {
            return
        }
        val code = runCatching { p.exitValue() }.getOrDefault(-1)
        val current = mss
        transition(
            when {
                current == ServerStatus.STOPPING -> ServerStatus.STOPPED
                current == ServerStatus.RESTARTING -> ServerStatus.STOPPED
                code == 0 -> ServerStatus.STOPPED
                else -> ServerStatus.ERROR
            },
            gen,
        )
    }

    /**
     * 状态转移的唯一入口。
     *
     * @param next 目标状态
     * @param gen  调用方认为的 generation；与当前不符则丢弃
     * @since VoidLand V0 | DEV.1
     */
    private fun transition(next: ServerStatus, gen: Int) {
        if (gen != generation.get()) return
        if (mss == next) return
        mss = next
    }

    private fun generateMuServerLock(){
        val lckFile = msi.msl.resolve("mksl.lck")
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true        // 解析时忽略未知字段
            isLenient = true                // 宽松解析（如允许非引号字符串）
            encodeDefaults = true           // 编码默认值
            explicitNulls = false           // 不编码 null 值
        }
        if(!lckFile.exists()) lckFile.createNewFile()
        lckFile.writeText(json.encodeToString(mspl))
    }

    /**
     * # MuServer Saver
     *
     * Save the MuServer Info (not only [MCJEServer.Info]) and Status as File when called.
     */
    fun save(){
        msc.getMuConfigInstance().set<Info>("MS_INF", msi)
        msc.getMuConfigInstance().set<MuServerProcessLck>("MS_LCK", mspl)
        msc.getMuConfigInstance().set<StartupConfig>("MS_SC", mssc)
    }

    @Serializable
    data class Info(
        val msid: String = randomMSID(),
        var name: String,
        val version: String,
        @Contextual val type: MCJEServerType,
        var desc: String,
        @Contextual var env: JavaEnvironment,
        var port: Int,
        @Contextual val msl: File = MuCoreMini.getMuCoreConfig().getServerFolder().resolve(name)
    ){init { require(port in 1..65535){"Port must be between 1 and 65535"} } }
    //He:后续可以添加试错或在前端解决，先抛异常

    @Serializable
    data class MuServerProcessLck(
        val pid: Long,
        @Contextual val startTime: LocalDateTime,
    )

    @Serializable
    data class StartupConfig(
        val minMemory: Int,
        val maxMemory: Int,
        val hasGui: Boolean = false,
        val jvmFlag: String = "",
    ){
        override fun toString(): String = StringBuilder().apply {
            append(" -Xms${minMemory}M")
            append(" -Xmx${maxMemory}M")
            if(jvmFlag.isNotEmpty()) append(" $jvmFlag")
            if(!hasGui) append(" --nogui")
        }.toString()
    }

    inner class Configuration{
        private val serverProperties: Properties = Properties()
        private val instances: MutableList<FileConfig> = mutableListOf()
        private val muConfigInstance: FileConfig = FileConfig
            .builder("MK-ServerLauncher.json")
            .autosave()
            .autoreload()
            .onFileNotFound { _, _ -> msi.msl.resolve("MK-ServerLauncher.json").createNewFile() }
            .build()

        fun getMuConfigInstance(): FileConfig = muConfigInstance

        fun getAvailablePaths2File(): List<File>{
            val paths: MutableList<File> = mutableListOf()
            msi.type.getSettingFiles().forEach { p ->
                val rawPath = msi.msl.resolve(p)
                if(rawPath.isFile){
                    paths.add(rawPath)
                }else{
                    warn(LOG_PREFIX, "MuServer ${msi.msid} Type Error: Unknown config path $rawPath")
                }
            }
            return paths
        }

        fun tryLoad(){
            val propsFile = msi.msl.resolve("server.properties")
            if (propsFile.exists()) {
                serverProperties.load(propsFile.reader())
            } else {
                warn(LOG_PREFIX, "server.properties canot found in ${msi.msl} , sodefaults")
            }

            getAvailablePaths2File().forEach { p ->
                instances.add(FileConfig.of(p))
            }
        }
    }
}



