package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.file.FileConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerLogPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerStatusPacket
import java.io.File
import java.io.PrintWriter
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
 * @since VoidLand V1 | DEV.1
 * @author Mu_Cloud
 */
class MCJEServer(
    // MuServer Basic Info
    val msi: Info,

    // MuServer Startup Config
    val mssc: StartupConfig,
){
    // MuServer Location
    val msl: File = MuCoreMini.getMuCoreConfig().getServerFolder().resolve(msi.name)

    // MuServer Core File
    val instance: File = msl.resolve("core.jar")

    // MuServer Status
    var mss: ServerStatus by Delegates.observable(ServerStatus.CREATED){ _, prev, current ->
        when(prev){
            ServerStatus.CREATED -> info(LOG_PREFIX, "Deploying ${msi.msid} Server.")
            ServerStatus.ERROR -> info(LOG_PREFIX, "${msi.msid} has been unlock and change to STOPPED Status. Please check the errors when running MuServer.")
            ServerStatus.RESTARTING -> info(LOG_PREFIX, "${msi.msid} has been restarted.")
            else -> info(LOG_PREFIX, "${msi.msid} Status changed from $prev to $current")
        }
        CoroutineScope(Dispatchers.IO).launch { sendPacket(MuServerStatusPacket(this@MCJEServer, current)) }
    } ;private set

    // MuTasks
    val muTaskPool: MutableList<String> = mutableListOf()

    // MuServer Event Channel
    val msec: MutableSharedFlow<MuServerPacket> = MutableSharedFlow()

    // Server Process
    private lateinit var msp: Process

    // Server Configuration
    private val msc: Configuration = Configuration(this)

    fun deploy() {
        info(LOG_PREFIX, "MuServer ${msi.msid} start deploying...")

        val rawCore = msi.type.getCoreFile(msi.version)
        instance.inputStream().copyTo(rawCore.outputStream())

        startMuServer()
        stopMuServer(enforce = true)

        Properties().apply {
            load(msl.resolve("eula.txt").reader())
            this["eula"] = "true"
            store(msl.resolve("eula.txt").writer(), null)
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
            if(enforce) msp.destroy() else {
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
            .directory(msl)
            .start()
            .also { p -> p.errorStream.bufferedReader().use { r ->
                sendPacket(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, r.readText()))
            }}
        msp.onExit()
            .orTimeout(60, TimeUnit.SECONDS)
            .thenAccept { p ->
                if(p.exitValue() == 0){
                    mss = ServerStatus.STOPPED
                }else{
                    mss = ServerStatus.ERROR
                }
            }.exceptionally { e ->
                if(e is TimeoutException){
                    msp.destroyForcibly()
                }
                null
            }
    }

    data class Info(
        val msid: String,
        var name: String,
        val version: String,
        val type: MCJEServerType,
        var desc: String,
        var env: JavaEnvironment,
        var port: Int,
    )

    class StartupConfig(
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

    class Configuration(
        val ms: MCJEServer
    ){
        private val serverProperties: Properties = Properties()
        private val instances: MutableList<FileConfig> = mutableListOf()
        private val muConfigInstance: FileConfig = FileConfig
            .builder("MK-ServerLauncher.yml")
            .autosave()
            .autoreload()
            .onFileNotFound { _, _ -> ms.msl.resolve("MK-ServerLauncher.yml").createNewFile() }
            .build()

        fun getAvailablePaths2File(): List<File>{
            val paths: MutableList<File> = mutableListOf()
            ms.msi.type.getSettingFiles().forEach { p ->
                val rawPath = ms.msl.resolve(p)
                if(rawPath.isFile){
                    paths.add(rawPath)
                }else{
                    warn(LOG_PREFIX, "MuServer ${ms.msi.msid} Type Error: Unknown config path $rawPath")
                }
            }
            return paths
        }

        fun tryLoad(){
            serverProperties.load(ms.msl.resolve("server.properties").reader())

            getAvailablePaths2File().forEach { p ->
                instances.add(FileConfig.of(p))
            }
        }
    }
}



