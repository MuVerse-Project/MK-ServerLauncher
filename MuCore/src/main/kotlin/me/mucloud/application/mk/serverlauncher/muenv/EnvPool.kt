package me.mucloud.application.mk.serverlauncher.muenv

import com.google.gson.reflect.TypeToken
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mucore.MuResult
import me.mucloud.application.mk.serverlauncher.mucore.MuStateResult
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool.envFile
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool.jEnvs
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 *  # Environment Pool
 *
 *  Supported to Install/Import/Delete MuEnvironment
 *
 *  @since VoidLand V1 | DEV.1
 *  @author Mu_Cloud
 */
object EnvPool {

    private const val LOG_PREFIX: String = "MuEnv.Pool"

    private val jEnvs: MutableList<JavaEnvironment> = mutableListOf() // In-Memory storage
    private val envFile: File = File("env.json") // Persistent storage file
    
    init {
        if(!envFile.exists()) {
            envFile.createNewFile()
        }
        scanLocalJavaEnv()
        scanRuntimeJavaEnv()
    }

    /**
     * # Local Java Environment Scanner
     *
     * Scan the System Java Installation as JavaEnvironment named "SysEnv"
     *
     * *For now, it will only scan the "JAVA_HOME" system environment to locate the Java Installation in System*
     *
     * This Function Implementation may change Frequently
     */
    private fun scanLocalJavaEnv(){
        val sysEnvPath = System.getenv("JAVA_HOME") ?: return
        addEnv("SysEnv", sysEnvPath)
    }

    private fun scanRuntimeJavaEnv(){
        val runtime = System.getProperty("java.home")
        if(runtime == null){
            warn(LOG_PREFIX, "Cannot get Java Runtime Environment in using")
        }
        addEnv("Runtime", runtime)
    }

    /**
     * Scan JavaEnvironment from env.json in MK-ServerLauncher Installation Folder
     *
     * @return List of JavaEnvironment, element deserialized by JavaEnvironmentAdapter
     */
    fun scanEnv() {
        if(envFile.exists()){
            gson.fromJson<List<JavaEnvironment>>(
                envFile.readText(StandardCharsets.UTF_8),
                object : TypeToken<List<JavaEnvironment>>(){}.type
            ).forEach{ e ->
                if(e.name != "Runtime" && e.name != "SysEnv"){
                    info(LOG_PREFIX, "Registered environment named ${e.name}, path ${e.path}")
                    addEnv(e.name, e.path.toString())
                }
            }
        }
    }

    /**
     * Write [jEnvs] Object to [envFile] by JavaEnvironmentAdapter
     */
    fun save(){
        if(envFile.exists()){
            envFile.writeText(gson.toJson(jEnvs, object : TypeToken<List<JavaEnvironment>>(){}.type))
        }
    }

    fun getEnv(name: String): MuResult<JavaEnvironment>{
        val target = jEnvs.find { it.name == name }
        return if(target == null){
            MuResult(false, null, "JavaEnvironment not found: $name")
        }else{
            MuResult(true, target)
        }
    }

    fun delEnv(envName: String): MuStateResult{
        val callback = getEnv(envName)
        if (!callback.isOk) { return MuStateResult(false, "Env could not be deleted: ${callback.msg}") }
        val env = callback.value!!
        if(env.name != "SysEnv" && env.name != "Runtime"){
            jEnvs.remove(env)
            save()
            return MuStateResult(true, "Deleted: ${env.name}")
        }else{
            return MuStateResult(false, "Env could not be deleted: \"SysEnv\" or \"Runtime\" Environment not allowed to delete")
        }
    }
    //He:关于这里我把因为忘了语法糖咋写了就重写成了正常风格

    private fun addEnv(name: String, path: String): MuStateResult{
        if(name.isEmpty() || name.isBlank()) return MuStateResult(false, "Invalid MuEnvironment Name: WhiteSpace or Empty are not allowed")
        if(!name.matches(Regex("^[A-Za-z].*"))) return MuStateResult(false, "Invalid MuEnvironment Name: Invalid Format")
        if(path.isEmpty() || path.isBlank()) return MuStateResult(false, "Invalid MuEnvironment Path: WhiteSpace or Empty are not allowed")

        val rawPath = Path.of(path)
        if(!rawPath.exists()) return MuStateResult(false, "Invalid MuEnvironment Path: Path Not Found: $path")
        if (!rawPath.isDirectory()) return MuStateResult(false, "Invalid MuEnvironment Path: Path Not a Directory: $path")

        jEnvs.forEach {
            if(it.name == name){
                return MuStateResult(false, "Ambiguous MuEnvironment Name: $name")
            }else if(it.getExecFolder().path == path){
                return MuStateResult(false, "Ambiguous MuEnvironment Path: $path")
            }
        }

        val rawEV = JavaEnvironment(name, rawPath)
        jEnvs.add(rawEV)
        return MuStateResult.OK
    }

    fun regEnv(name: String, path: String): MuStateResult = addEnv(name, path).also { if(it.isOk) save() }

    fun getEnvList(): List<JavaEnvironment> = jEnvs

}