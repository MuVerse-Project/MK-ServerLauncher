package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.file.FileConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import java.io.File
import java.util.*

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
    var mss: ServerStatus = ServerStatus.STOPPED
        private set

    // MuTasks
    val muTaskPool: MutableList<String> = mutableListOf()

    // MuServer Event Channel
    val msec: MutableSharedFlow<MuPacket> = MutableSharedFlow()

    // Server Process
    lateinit var msp: Process

    // Server Configuration
    lateinit var msc: Configuration

    fun deploy() {

    }

    fun runMuTasks(){

    }

    fun startMuServer(){
        if(mss == ServerStatus.STOPPED){
            mss = ServerStatus.PREPARING
            runMuTasks()
            msp = ProcessBuilder("${msi.env.getAbsoluteExecPath()} -jar $mssc ${instance.absolutePath}").start()
        }
    }

    fun stopMuServer(enforce: Boolean = false){
        if(mss == ServerStatus.RUNNING){
            mss = ServerStatus.STOPPING
            if(enforce) msp.destroy() else msp.onExit()
            mss = ServerStatus.STOPPED
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
        ms: MCJEServer
    ){
        private val serverProperties: Properties = Properties()
        private val instances: List<FileConfig> = mutableListOf()

        fun tryLoad(){

        }

        suspend fun waitLoad(){

        }
    }
}



