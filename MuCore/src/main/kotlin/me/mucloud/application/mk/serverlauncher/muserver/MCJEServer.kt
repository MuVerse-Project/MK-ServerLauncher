package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.file.FileConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerLogPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerStatusPacket
import java.io.File
import java.util.*
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
        info(LOG_PREFIX, "MuServer ${msi.MSID} Status changed from $prev to $current")
        CoroutineScope(Dispatchers.IO).launch {
            sendPacket(MuServerStatusPacket(this@MCJEServer, current))
        }
    }
        private set

    // MuTasks
    val muTaskPool: MutableList<String> = mutableListOf()

    // MuServer Event Channel
    val msec: MutableSharedFlow<MuServerPacket> = MutableSharedFlow()

    // Server Process
    lateinit var msp: Process

    // Server Configuration
    lateinit var msc: Configuration

    fun deploy() {
        info(LOG_PREFIX, "MuServer ${msi.MSID} start deploying...")

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

    fun runMuTasks(){
        muTaskPool.forEach { tsk ->
            val proc = ProcessBuilder(tsk).start()
            proc.errorStream.bufferedReader().use {
                info("MuServer-${msi.MSID}-MuTask", "Running MuTask.")
                sendPacket(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, it.readText()))
            }
        }
    }

    fun startMuServer(){
        if(mss == ServerStatus.STOPPED){
            mss = ServerStatus.PREPARING
            runMuTasks()
            msp = ProcessBuilder("${msi.env.getAbsoluteExecPath()} -jar $mssc ${instance.absolutePath}").start()
            msp.errorStream.bufferedReader().use {
                sendPacket(MuServerLogPacket(this@MCJEServer, MuServerLogPacket.LogLevel.INFO, it.readText()))
            }
        }
    }

    fun stopMuServer(enforce: Boolean = false){
        if(mss == ServerStatus.RUNNING){
            mss = ServerStatus.STOPPING
            if(enforce) msp.destroy() else msp.onExit()
            mss = ServerStatus.STOPPED
        }
    }

    fun sendPacket(packet: MuServerPacket){
        CoroutineScope(Dispatchers.IO).launch {
            msec.emit(packet)
        }
    }

    data class Info(
        val MSID: String,
        var name: String,
        val version: String,
        val type: MCJEServerType,
        var desc: String,
        var env: JavaEnvironment,
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
            if(jvmFlag.isNotEmpty()){
                append(" $jvmFlag")
            }
            if(!hasGui){
                append(" --nogui")
            }
        }.toString()
    }

    class Configuration(
        val ms: MCJEServer
    ){
        private val serverProperties: Properties = Properties()
        private val instances: List<FileConfig> = mutableListOf()

        fun tryLoad(){
            serverProperties.load(ms.msl.resolve("server.properties").reader())

        }
    }
}



