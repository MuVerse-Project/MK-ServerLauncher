package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.err
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * # | MuExtension - MCJEServer
 * ## MC Java Edition Server
 *
 * @since DEV.1
 * @author Mu_Cloud
 */
class MCJEServer(
    private var name: String,
    private var version: String,
    private var type: MCJEServerType,
    private var desc: String,
    private var port: Int,
    private var env: JavaEnvironment,
){
    private var location: File = File(MuCoreMini.getMuCoreConfig().getServerFolder(), name)
    private var status: ServerStatus = ServerStatus.STOPPED
    private val totalFailCount: Int = 0
    private val totalPassCount: Int = 0
    private val dataFlow = MutableSharedFlow<JsonObject>()
    private var beforeWorks: MutableList<String> = mutableListOf()
    private lateinit var config: MCJEServerConfig

    private lateinit var ServerProcess: Process

    init{
        if(!location.exists()){
            location.mkdirs()
        }
        config.load()
    }

    // Verify the availability of the MCJEServer
    private fun verify(){
        if(!location.exists()){
            runBlocking { dataFlow.emit(TODO()) }
        }
    }

    fun deploy(){
        // todo: Deploy Task
        config = MCJEServerConfig()
    }

    // Before Tasks.
    fun regBeforeWork(work: String) = beforeWorks.add(work)
    fun regBeforeWork(vararg work: String) = beforeWorks.addAll(work)

    fun runBeforeWorks(){
        beforeWorks.forEach { be ->
            try {
                ServerProcess = Runtime.getRuntime().exec(be).apply {
                    errorStream.bufferedReader().forEachLine { l ->
                        runBlocking {
                            dataFlow.emit(JsonObject().also { j ->
                                j.addProperty("type", "console.out:before_work")
                                j.add("data", JsonObject().also { data ->
                                    data.addProperty("index", beforeWorks.indexOf(be))
                                    data.addProperty("msg", l)
                                })
                            })
                        }
                    }
                }
            }catch (e: Exception) {
                CoroutineScope(Dispatchers.IO).launch { dataFlow.emit(TODO()) }
            }
        }
    }

    fun start() {
        verify()
        status = ServerStatus.PREPARING
        runBeforeWorks()
        val cmd = "${env.getAbsoluteExecPath()} -Xms${config.getMinMemory()} -Xmx${config.getMaxMemory()} ${config.getAdditionalVMFlags()} -jar core.jar"
        ServerProcess = ProcessBuilder(cmd).directory(getFolder()).start().apply {
            errorStream.bufferedReader(UTF_8).forEachLine { l ->
                runBlocking {
                    dataFlow.emit(JsonObject().apply {
                        addProperty("type", "console.out:info")
                        addProperty("msg", l)
                    })
                }
            }
        }
    }

    fun stop() {
        status = ServerStatus.STOPPING
        ServerProcess.destroy()
        val comp = ServerProcess.waitFor(5000, TimeUnit.MILLISECONDS)
        if(comp){
            status = ServerStatus.STOPPED
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                dataFlow.emit(TODO())
            }
        }
    }

    fun getName(): String = name
    fun setName(name: String) { this.name = name }
    fun getStatus(): ServerStatus = status
    fun totalFailCount(): Int = totalFailCount
    fun totalPassCount(): Int = totalPassCount
    fun getDescription(): String = desc
    fun setDescription(desc: String){ this.desc = desc }
    fun getFolder(): File = location
    fun getPort(): Int = port
    fun setPort(port: Int) { this.port = port }
    fun getVersion(): String = version
    fun getType(): MCJEServerType = type
    fun getEnv(): JavaEnvironment = env
    fun setEnv(env: JavaEnvironment) { this.env = env }
    fun lastLaunchTime(): LocalDateTime = LocalDateTime.now() // todo now
    fun getConfig(): MCJEServerConfig = config
    fun getBeforeWorks(): List<String> = beforeWorks

    fun save(){
        FileWriter(File(getFolder(), "MuServer.json").also { if(!it.exists()) it.createNewFile() }, UTF_8).also {
            it.write(GsonBuilder().setPrettyPrinting().create().toJson(this))
            it.flush()
        }
    }

    fun getServerFlow() = dataFlow
    fun sendCommand(cmd: String){ TODO("Implementation in iLoveMu") }
    fun sendMsg(msg: String){ TODO("Implementation in iLoveMu") }

    fun sendPacket(mp: MuPacket){
        CoroutineScope(Dispatchers.IO).launch {
            dataFlow.emit(mp.toJson())
        }
    }

    /**
     * # | MCJEServerConfig
     *
     * TODO: Change to Embedded Configuration
     */
    inner class MCJEServerConfig{
        private var minMemory: Int = 1024 // MB, Def
        private var maxMemory: Int = 1024 // MB, Def
        private var additionalVMFlags: String = ""

        private val serverProperties = Properties().apply { load(FileReader(getFolder().resolve("server.properties"))) }
        private val rawServerConfig: MutableList<FileConfig> = mutableListOf()
        private val muServerConfig: FileConfig = FileConfig.of(getFolder().resolve("MuServer-Config.yml"))

        fun load(){
            initMuServerConfig()
            initRawServerConfig()
        }

        private fun initMuServerConfig(){
            val muserverConfigFile = getFolder().resolve("MuServer-Config.yml")
            if(!muserverConfigFile.exists()){
                muserverConfigFile.createNewFile()
            }
        }

        private fun initRawServerConfig(){
            type.getServerCoreSettingsFile().forEach { p ->
                val targetFile = getFolder().toPath().resolve(p).toFile()
                if(targetFile.exists()){
                    rawServerConfig.add(FileConfig.of(targetFile).also { it.load() })
                }else{
                    warn("${targetFile.absolutePath} is not a valid MuServer config file")
                }
            }
        }

        // Raw Server Configurations
        fun getAll(): List<FileConfig> = rawServerConfig

        fun getAll(fileName: String): FileConfig{
            rawServerConfig.forEach { c ->
                if(fileName == c.file.name){
                    return c
                }
            }
            val thrown = UnsupportedOperationException()
            err("The config File ($fileName) is not found in MuServer Configuration", thrown)
            throw thrown
        }


        fun setProperty(fileName: String, key: String, value: String) {
            val target = getAll(fileName)
            target.set<String>(key, value)
        }

        fun getProperty(fileName: String, key: String): String{
            val target = getAll(fileName)
            return target.get(key)
        }

        // MuServer Config
        fun setMuServerProperty(key: String, value: String){
            muServerConfig.set<String>(key, value)
        }

        fun getMuServerConfig(): Config = muServerConfig
        fun delMuServerConfig(key: String){
            muServerConfig.remove<String>(key)
        }

        // Server Properties
        fun getServerProperties(): Properties = serverProperties
        fun setServerProperties(key: String, value: String){
            serverProperties[key] = value
        }

        fun getMinMemory(): Int = minMemory
        fun getMaxMemory(): Int = maxMemory
        fun getAdditionalVMFlags(): String = additionalVMFlags
        fun save(){

        }
    }

}



