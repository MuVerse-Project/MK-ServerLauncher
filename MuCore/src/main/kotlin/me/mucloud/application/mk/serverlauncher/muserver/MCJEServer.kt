package me.mucloud.application.mk.serverlauncher.muserver

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.err
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
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
    companion object {
        const val SERVER_META_FILE = "MK-ServerLauncher.json"
        const val LEGACY_SERVER_META_FILE = "MuServer.json"
    }
    private var location: File = File(MuCoreMini.getMuCoreConfig().getServerFolder(), name)
    private var status: ServerStatus = ServerStatus.STOPPED
    private val totalFailCount: Int = 0
    private val totalPassCount: Int = 0
    private val dataFlow = MutableSharedFlow<JsonObject>()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var beforeWorks: MutableList<String> = mutableListOf()
    private lateinit var config: MCJEServerConfig

    private lateinit var serverProcess: Process
    private var processWriter: BufferedWriter? = null
    private var stdoutJob: Job? = null
    private var stderrJob: Job? = null
    private var lastLaunchAt: LocalDateTime? = null

    init{
        if(!location.exists()){
            location.mkdirs()
        }
        config = MCJEServerConfig()
        config.load()
    }

    // Verify the availability of the MCJEServer
    private fun verify(): Boolean {
        if(!location.exists() && !location.mkdirs()){
            emit("console.out:error", "Server folder is unavailable: ${location.absolutePath}")
            status = ServerStatus.IN_ERROR
            return false
        }
        return true
    }

    fun deploy(){
        try {
            val core = type.getServerCore(version)
            val target = getFolder().resolve("core.jar")
            if (core.absolutePath != target.absolutePath) {
                target.parentFile?.mkdirs()
                core.copyTo(target, overwrite = true)
            }
            val eula = getFolder().resolve("eula.txt")
            if (!eula.exists()) {
                eula.writeText("eula=true\n", UTF_8)
            }
            config = MCJEServerConfig().also { it.load() }
        } catch (e: Exception) {
            status = ServerStatus.IN_ERROR
            emit("console.out:error", "Deploy failed: ${e.message ?: "Unknown Error"}")
        }
    }

    private fun emit(type: String, msg: String) {
        runBlocking {
            dataFlow.emit(JsonObject().apply {
                addProperty("type", type)
                addProperty("msg", msg)
            })
        }
    }

    // Before Tasks.
    fun regBeforeWork(work: String) = beforeWorks.add(work)
    fun regBeforeWork(vararg work: String) = beforeWorks.addAll(work)

    fun runBeforeWorks(){
        beforeWorks.forEachIndexed { index, be ->
            try {
                val process = Runtime.getRuntime().exec(be)
                process.errorStream.bufferedReader().forEachLine { l ->
                    runBlocking {
                        dataFlow.emit(JsonObject().also { j ->
                            j.addProperty("type", "console.out:before_work")
                            j.add("data", JsonObject().also { data ->
                                data.addProperty("index", index)
                                data.addProperty("msg", l)
                            })
                        })
                    }
                }
            }catch (e: Exception) {
                emit("console.out:before_work_err", e.message ?: "Unknown Error")
                status = ServerStatus.IN_ERROR
            }
        }
    }

    fun start() {
        if (status == ServerStatus.RUNNING) return
        if (!verify()) return

        status = ServerStatus.PREPARING
        runBeforeWorks()
        if (status == ServerStatus.IN_ERROR) return

        val command = mutableListOf(
            env.getAbsoluteExecPath(),
            "-Xms${config.getMinMemory()}M",
            "-Xmx${config.getMaxMemory()}M"
        )
        val additional = config.getAdditionalVMFlags().trim()
        if (additional.isNotEmpty()) {
            command.addAll(additional.split("\\s+").filter { it.isNotBlank() })
        }
        command.addAll(listOf("-jar", "core.jar", "nogui"))

        serverProcess = ProcessBuilder(command)
            .directory(getFolder())
            .redirectErrorStream(false)
            .start()
        processWriter = serverProcess.outputStream.bufferedWriter(UTF_8)

        stdoutJob = ioScope.launch {
            serverProcess.inputStream?.let { ins ->
                InputStreamReader(ins, UTF_8).buffered().forEachLine { emit("console.out:info", it) }
            }
        }
        stderrJob = ioScope.launch {
            serverProcess.errorStream?.let { ins ->
                InputStreamReader(ins, UTF_8).buffered().forEachLine { emit("console.out:error", it) }
            }
        }

        status = ServerStatus.RUNNING
        lastLaunchAt = LocalDateTime.now()
    }

    fun stop() {
        if (status != ServerStatus.RUNNING && !::serverProcess.isInitialized) return
        status = ServerStatus.STOPPING
        try {
            sendCommand("stop")
            val completed = serverProcess.waitFor(5000, TimeUnit.MILLISECONDS)
            if (!completed) {
                serverProcess.destroy()
            }
            status = ServerStatus.STOPPED
        } catch (e: Exception) {
            status = ServerStatus.IN_ERROR
            emit("console.out:error", "Stop failed: ${e.message ?: "Unknown Error"}")
        } finally {
            stdoutJob?.cancel()
            stderrJob?.cancel()
            stdoutJob = null
            stderrJob = null
            processWriter = null
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
    fun lastLaunchTime(): LocalDateTime = lastLaunchAt ?: LocalDateTime.MIN
    fun getConfig(): MCJEServerConfig = config
    fun getBeforeWorks(): List<String> = beforeWorks

    fun save(){
        FileWriter(File(getFolder(), SERVER_META_FILE).also { if(!it.exists()) it.createNewFile() }, UTF_8).also {
            it.write(GsonBuilder().setPrettyPrinting().create().toJson(this))
            it.flush()
        }
    }

    fun getServerFlow() = dataFlow
    fun sendCommand(cmd: String){
        if (status != ServerStatus.RUNNING || !::serverProcess.isInitialized) return
        processWriter?.apply {
            write(cmd)
            newLine()
            flush()
        }
    }
    fun sendMsg(msg: String){ sendCommand("say $msg") }

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

        private var serverProperties: Properties = Properties()
        private val rawServerConfig: MutableList<FileConfig> = mutableListOf()
        private val muServerConfig: FileConfig = FileConfig.of(getFolder().resolve("MuServer-Config.yml"))

        fun load(){
            initMuServerConfig()
            initServerProperties()
            initRawServerConfig()
        }

        private fun initMuServerConfig(){
            val muserverConfigFile = getFolder().resolve("MuServer-Config.yml")
            if(!muserverConfigFile.exists()){
                muserverConfigFile.createNewFile()
            }
            muServerConfig.load()
            minMemory = muServerConfig.getOptional<Int>("vm.minMemory").orElse(minMemory)
            maxMemory = muServerConfig.getOptional<Int>("vm.maxMemory").orElse(maxMemory)
            additionalVMFlags = muServerConfig.getOptional<String>("vm.additionalFlags").orElse(additionalVMFlags)
        }

        private fun initServerProperties(){
            val serverPropertiesFile = getFolder().resolve("server.properties")
            if(!serverPropertiesFile.exists()){
                serverPropertiesFile.createNewFile()
            }
            serverProperties = Properties().apply {
                FileReader(serverPropertiesFile).use { load(it) }
            }
        }

        private fun initRawServerConfig(){
            rawServerConfig.clear()
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
            when(key){
                "vm.minMemory" -> minMemory = value.toIntOrNull() ?: minMemory
                "vm.maxMemory" -> maxMemory = value.toIntOrNull() ?: maxMemory
                "vm.additionalFlags" -> additionalVMFlags = value
            }
        }

        fun getMuServerConfig(): Config = muServerConfig
        fun delMuServerConfig(key: String){
            muServerConfig.remove<String>(key)
            when(key){
                "vm.minMemory" -> minMemory = 1024
                "vm.maxMemory" -> maxMemory = 1024
                "vm.additionalFlags" -> additionalVMFlags = ""
            }
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
            muServerConfig.set<Int>("vm.minMemory", minMemory)
            muServerConfig.set<Int>("vm.maxMemory", maxMemory)
            muServerConfig.set<String>("vm.additionalFlags", additionalVMFlags)
            muServerConfig.save()

            rawServerConfig.forEach { it.save() }

            FileWriter(getFolder().resolve("server.properties"), UTF_8).use { writer ->
                serverProperties.store(writer, "MCJEServer server.properties")
            }
        }
    }

}



