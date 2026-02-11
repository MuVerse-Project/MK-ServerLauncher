package me.mucloud.application.mk.serverlauncher.common.server.mcserver

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.mucloud.application.mk.serverlauncher.common.env.EnvPool
import me.mucloud.application.mk.serverlauncher.common.env.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.common.manage.ConfigurationFactory
import me.mucloud.application.mk.serverlauncher.common.server.ServerPool
import java.nio.charset.StandardCharsets.UTF_8

/**
 * MC Java Edition Server.
 *
 * MuPack Internal Server Template
 *
 * @since DEV.1
 * @author Mu_Cloud
 */
data class MCJEServer(
    private var name: String,
    private val version: String,
    private var type: MCJEServerType,
    private var desc: String,
    private var port: Int = 25565,
    private var env: JavaEnvironment,
    @SerializedName("before_works") private var beforeWork: MutableList<String> = mutableListOf(),
){
    private var location: File = File(ConfigurationFactory.getConfiguration().getServerFolder(), name)
    private var running: Boolean = false
    private val totalFailCount: Int = 0
    private val totalPassCount: Int = 0
    private val config: MCJEServerConfig = MCJEServerConfig(this)
    private val dataFlow = MutableSharedFlow<JsonObject>()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var lastLaunchAt: LocalDateTime? = null

    @Transient private var process: Process? = null
    @Transient private var processWriter: BufferedWriter? = null
    @Transient private var stdoutJob: Job? = null
    @Transient private var stderrJob: Job? = null

    init{
        if(!location.exists()){
            location.mkdirs()
        }
    }

    fun deploy(){
        try {
            val core = type.getServerCore(version)
            val target = getFolder().resolve("core.jar")
            if (core.absolutePath != target.absolutePath) {
                target.parentFile?.mkdirs()
                java.nio.file.Files.copy(
                    core.toPath(),
                    target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
            }
            val eulaFile = getFolder().resolve("eula.txt")
            if (!eulaFile.exists()) {
                eulaFile.writeText("eula=true\n", StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            emit("console.out:error", "Deploy failed: ${e.message ?: "Unknown Error"}")
        }
    }

    private fun emit(type: String, msg: String){
        runBlocking {
            dataFlow.emit(JsonObject().apply {
                addProperty("type", type)
                addProperty("msg", msg)
            })
        }
    }

    private fun getServerJar(): File? {
        val preferred = getFolder().resolve("core.jar")
        return preferred.takeIf { it.exists() }
    }

    fun regBeforeWork(work: String) = beforeWork.add(work)
    fun regBeforeWork(vararg work: String) = beforeWork.addAll(work)

    fun runBeforeWorks(): Int{
        beforeWork.forEach { be ->
            try {
                Runtime.getRuntime().exec(be).errorStream.bufferedReader().forEachLine { l ->
                    runBlocking { dataFlow.emit(JsonObject().also { j ->
                        j.addProperty("type", "console.out:before_work")
                        j.add("data", JsonObject().also { data ->
                            data.addProperty("index", beforeWork.indexOf(be))
                            data.addProperty("msg", l)
                        })
                    })}
                }
            }catch (e: Exception) {
                runBlocking {
                    dataFlow.emit(JsonObject().also { j ->
                        j.addProperty("type", "console.out:before_work_err")
                        j.add("data", JsonObject().also { data ->
                            data.addProperty("index", beforeWork.indexOf(be))
                            data.addProperty("msg", e.message ?: "Unknown Error")
                        })
                    })
                }
                return 1
            }
        }
        return 0
    }

    fun start() {
        if (running) return
        if (runBeforeWorks() != 0) return

        val jar = getServerJar() ?: run {
            emit("console.out:error", "Server jar not found. Please deploy/download server core first.")
            return
        }

        val javaExec = env.getAbsoluteExecPath()
        val command = listOf(javaExec, "-jar", jar.absolutePath, "nogui")

        val pb = ProcessBuilder(command)
            .directory(getFolder())
            .redirectErrorStream(false)

        process = pb.start().also {
            processWriter = it.outputStream.bufferedWriter(UTF_8)
        }
        running = true
        lastLaunchAt = LocalDateTime.now()

        stdoutJob = ioScope.launch {
            process?.inputStream?.let { ins ->
                InputStreamReader(ins, UTF_8).buffered().forEachLine { l ->
                    emit("console.out:info", l)
                }
            }
        }

        stderrJob = ioScope.launch {
            process?.errorStream?.let { ins ->
                InputStreamReader(ins, UTF_8).buffered().forEachLine { l ->
                    emit("console.out:error", l)
                }
            }
        }
    }

    fun stop() {
        if (!running) return
        try {
            sendCommand("stop")
            process?.waitFor()
        } catch (_: Exception) {
            process?.destroy()
        } finally {
            running = false
            stdoutJob?.cancel()
            stderrJob?.cancel()
            stdoutJob = null
            stderrJob = null
            processWriter = null
            process = null
        }
    }

    fun getName(): String = name
    fun setName(name: String) { this.name = name }

    fun isRunning(): Boolean = running

    fun totalFailCount(): Int = totalFailCount

    fun totalPassCount(): Int = totalPassCount

    fun getDescription(): String = desc
    fun setDescription(desc: String){ this.desc = desc }

    fun getFolder(): File = location
    fun setFolder(loc: File){
        if(loc.exists() && loc.isDirectory){
            location = loc
        }else{
            throw RuntimeException("Server Folder Not Found")
        }
    }

    fun getPort(): Int = port
    fun setPort(port: Int) { this.port = port }

    fun getVersion(): String = version

    fun getType(): MCJEServerType = type

    fun getEnv(): JavaEnvironment = env
    fun setEnv(env: JavaEnvironment) { this.env = env }

    fun lastLaunchTime(): LocalDateTime = lastLaunchAt ?: LocalDateTime.MIN

    fun getConfig(): MCJEServerConfig = MCJEServerConfig(this)
    fun getBeforeWorks(): List<String> = beforeWork

    fun saveToFile(){
        FileWriter(File(getFolder(), "MK-ServerLauncher.json").also { if(!it.exists()) it.createNewFile() }, StandardCharsets.UTF_8).also {
            it.write(GsonBuilder().setPrettyPrinting().create().toJson(this))
            it.flush()
        }
    }

    fun getServerFlow() = dataFlow

    fun sendCommand(cmd: String){
        if (!running) return
        processWriter?.apply {
            write(cmd)
            newLine()
            flush()
        }
    }

    fun sendMsg(msg: String){
        sendCommand("say $msg")
    }
}

/**
 * # | MCJEServerConfig
 */
class MCJEServerConfig(
    server: MCJEServer
){
    private val rawServerPropertiesFile: File = server.getFolder().resolve("server.properties")
    private val rawServerProperties: Properties = Properties().apply {
        if(rawServerPropertiesFile.exists()){
            load(rawServerPropertiesFile.reader(StandardCharsets.UTF_8))
        }
    }

    fun setProperty(key: String, value: String) = rawServerProperties.setProperty(key, value)
    fun delProperty(key: String) = rawServerProperties.remove(key)
    fun save() {
        rawServerPropertiesFile.parentFile?.mkdirs()
        if(!rawServerPropertiesFile.exists()) rawServerPropertiesFile.createNewFile()
        rawServerProperties.store(rawServerPropertiesFile.writer(StandardCharsets.UTF_8), null)
    }
}

object MCJEServerAdapter: JsonSerializer<MCJEServer>, JsonDeserializer<MCJEServer>{
    override fun serialize(
        s: MCJEServer,
        t: Type,
        c: JsonSerializationContext
    ): JsonElement {
        return JsonObject().apply {
            addProperty("name", s.getName())
            addProperty("desc", s.getDescription())
            addProperty("version", s.getVersion())
            addProperty("type", s.getType().id)
            addProperty("port", s.getPort())
            addProperty("env", s.getEnv().getName())
            add("before_works", c.serialize(s.getBeforeWorks()))
            addProperty("location", s.getFolder().absolutePath)
        }
    }

    override fun deserialize(
        j: JsonElement,
        t: Type,
        c: JsonDeserializationContext
    ): MCJEServer {
        j.asJsonObject.also { v ->
            return MCJEServer(
                v["name"].asString,
                v["version"].asString,
                ServerPool.getType(v["type"].asString),
                v["desc"].asString,
                v["port"].asInt,
                EnvPool.getEnv(v["env"].asString) as JavaEnvironment,
                c.deserialize(v["before_works"], MutableList::class.java),
            ).apply { setFolder(File(v["location"].asString)) }
        }
    }
}