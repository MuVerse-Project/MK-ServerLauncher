package me.mucloud.application.mk.serverlauncher.muenv

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.mucloud.application.mk.serverlauncher.mucore.MuResult
import me.mucloud.application.mk.serverlauncher.mucore.MuStateResult
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool.envFile
import me.mucloud.application.mk.serverlauncher.muenv.EnvPool.jEnvs
import java.io.File
import java.nio.charset.StandardCharsets

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

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(JavaEnvironment::class.java, JavaEnvironmentAdapter)
        .create()

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
        regEnv(JavaEnvironment("SysEnv", sysEnvPath))
    }

    private fun scanRuntimeJavaEnv(){
        val runtime = System.getProperty("java.home")
        if(runtime == null){
            warn(LOG_PREFIX, "Cannot get Java Runtime Environment in using")
        }
        regEnv(JavaEnvironment("Runtime", runtime))
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
                    regEnv(e)
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
        return if(!callback.isOk){
            MuStateResult(false, "Env could not be delated: ${callback.msg}")
        }else{
            MuStateResult.OK
        }
    }

    fun regEnv(env: JavaEnvironment): MuStateResult{
        val target = jEnvs.find { it.name == env.name || it.getAbsoluteExecPath() == env.getAbsoluteExecPath() }
        return if(target != null){
            MuStateResult(false, "Env could not be registered: Env name or location exists")
        }else{
            MuStateResult.OK
        }
    }

    fun getEnvList(): List<JavaEnvironment> = jEnvs

}