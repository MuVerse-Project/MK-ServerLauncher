package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.file.FileConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerLogPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerStatusPacket
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool.randomMSID
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.properties.Delegates

private const val LOG_PREFIX: String = "MuServer"

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
        CoroutineScope(Dispatchers.IO).launch { sendPacket(MuServerStatusPacket(this@MCJEServer, current)) }
    }; private set

    // MuServer Process Lock
    var mspl: MuServerProcessLck? = null
        private set

    // MuTasks
    val muTaskPool: MutableList<String> = mutableListOf()

    // MuServer Event Channel
    val msec: MutableSharedFlow<MuServerPacket> = MutableSharedFlow()

    // Server Process
    private lateinit var msp: Process

    // Server Configuration
    val msc: Configuration = Configuration()

    fun deploy() {
        info(LOG_PREFIX, "MuServer ${msi.msid} start deploying...")

        val rawCore = msi.type.getCoreFile(msi.version)
        instance.inputStream().copyTo(rawCore.outputStream())

        startMuServer()
        stopMuServer(enforce = true)

        Properties().apply {
            load(msi.msl.resolve("eula.txt").reader())
            this["eula"] = "true"
            store(msi.msl.resolve("eula.txt").writer(), null)
        }

        msc.tryLoad()
    }

    fun runMuTasks(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            muTaskPool.forEach { tsk ->
                val proc = ProcessBuilder(tsk).start()
                proc.errorStream.bufferedReader().use {
                    info("MuServer-${msi.msid}-MuTask", "Running MuTask.")
                    sendPacket(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, it.readText()))
                }
                if (proc.waitFor() == 0) {
                    sendPacket(
                        MuServerLogPacket(
                            this@MCJEServer,
                            MuServerLogPacket.LogLevel.INFO,
                            "MuServer-${msi.msid}-MuTask finished"
                        )
                    )
                } else {
                    sendPacket(
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

    fun startMuServer(){
        if(mss == ServerStatus.ERROR){
            mss = ServerStatus.STOPPED
        }else if(mss == ServerStatus.STOPPED){
            mss = ServerStatus.PREPARING
            runMuTasks().get()
            runProcess()
            mss = ServerStatus.RUNNING
        }
    }

    fun stopMuServer(enforce: Boolean = false){ // TDOD: re-check: Need Enforce?
        if(mss == ServerStatus.RUNNING){
            mss = ServerStatus.STOPPING
            if(enforce) msp.destroyForcibly() else {
                sendMessage("Server Stopping.")
                sendCommand("stop")
            }
            mss = ServerStatus.STOPPED
        }
    }

    fun restartMuServer(){
        if(mss == ServerStatus.RUNNING){
            mss = ServerStatus.RESTARTING
            sendMessage("Server Restarting.")
            msp.destroy()
            runProcess()
            mss = ServerStatus.RUNNING
        }
    }

    fun sendMessage(msg: String){
        sendCommand("say [MKSL] $msg")
    }

    fun sendCommand(cmd: String){
        if(mss == ServerStatus.RUNNING){
            PrintWriter(msp.outputStream, true).use { pw -> pw.println(cmd) }
        }
    }

    fun sendPacket(packet: MuServerPacket){
        CoroutineScope(Dispatchers.IO).launch {
            msec.emit(packet)
        }
    }

    private fun runProcess(){
        msp = ProcessBuilder("${msi.env.getAbsoluteExecPath()} -jar $mssc ${instance.absolutePath}")
            .directory(msi.msl)
            .redirectOutput(msi.msl.resolve("mksl-${msi.msid}.log"))
            .start()
            .also { p -> p.errorStream.bufferedReader().use { r ->
                sendPacket(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, r.readText()))
            }}

        mspl = MuServerProcessLck(msp.pid(), LocalDateTime.now())
        generateMuServerLock()

        msp.onExit()
            .orTimeout(60, TimeUnit.SECONDS)
            .thenAccept { p ->
                mss = if(p.exitValue() == 0){
                    ServerStatus.STOPPED
                }else{
                    ServerStatus.ERROR
                }
            }.exceptionally { e ->
                if(e is TimeoutException){
                    msp.destroyForcibly()
                }
                null
            }
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
    )

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
            serverProperties.load(msi.msl.resolve("server.properties").reader())

            getAvailablePaths2File().forEach { p ->
                instances.add(FileConfig.of(p))
            }
        }
    }
}



