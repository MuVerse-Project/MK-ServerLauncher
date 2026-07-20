package me.mucloud.application.mk.serverlauncher.muenv

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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
        scanRuntimeJavaEnv()
        scanLocalJavaEnv()
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
    private fun scanLocalJavaEnv(): Boolean{
        val sysEnvPath = System.getenv("JAVA_HOME") ?: return false
        regEnv(JavaEnvironment("SysEnv", sysEnvPath))
        return true
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
                jEnvs.add(e)
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

    fun getEnv(name: String) = jEnvs.find { it.name == name }

    fun delEnv(envName: String): Boolean{
        return jEnvs.removeIf { it.name == envName }.also{ save() }
    }

    fun regEnv(env: JavaEnvironment){
        val target = jEnvs.find { it.name == env.name || it.getExecFolder() == env.getExecFolder() }
        if (target == null){
            if (!jEnvs.add(env)) warn(LOG_PREFIX, "Cannot register the Java Environment in ${env.name} because it has been registered.")
            save()
        }
    }

    fun getEnvList(): List<JavaEnvironment> = jEnvs

}