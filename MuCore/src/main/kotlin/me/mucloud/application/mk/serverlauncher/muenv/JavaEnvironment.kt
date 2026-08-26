package me.mucloud.application.mk.serverlauncher.muenv

import java.io.File
import java.io.FileReader
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * # | MuExtension - MCJEServer
 *
 * ## Java Environment
 *
 * @since DEV.1
 * @author Mu_Cloud
 * @param name JavaEnvironment Name
 * @param path Java Installation Folder (like %JAVA_HOME% Folder)
 */
class JavaEnvironment(
    val name: String,
    val path: Path,
){

    private val distributionVer: String

    init{
        require(path.exists()) { "Path $path does not exist." }
        require(path.isDirectory()) { "Path $path is not a directory." }
        val map = getReleaseFileContent()
        distributionVer = "${map["JAVA_VERSION"] ?: "Unknown"} (${map["IMPLEMENTOR_VERSION"] ?: "Unknown"})"
    }

    /**
     * Get the Content of File named "RELEASE" in the Java Installation Folder
     *
     * @return The File Content as Map
     */
    private fun getReleaseFileContent(): Map<String, String>{
        val map = mutableMapOf<String, String>()
        val releaseFile = path.resolve("release")
        if(!releaseFile.exists()) return map
        FileReader(releaseFile.toFile()).readLines().forEach { l ->
            val split = l.split("=")
            map[split[0]] = split[1].trim('\"')
        }
        return map
    }

    /**
     * Get the Version of JavaEnvironment
     *
     * @return the version of JavaEnvironment as [String]
     */
    fun getVersionString(): String = distributionVer

    /**
     * Get the Version of JavaEnvironment
     *
     * @return the version of JavaEnvironment as [JavaVersion]
     */
    fun getVersion(): JavaVersion = JavaVersion.getInstance(this)

    /**
     * Get the Java Environment Path as [File]
     *
     * @return The Java Environment Folder as [File]
     */
    fun getExecFolder(): File = path.toFile()

    /**
     * Get the Executable File
     *
     * @return The executable file of JavaEnvironment, which usually refers to the "java.exe" file
     */
    fun getAbsoluteExecPath(): String {
        val name = when{
            System.getProperty("os.name").lowercase().contains("win") -> "java.exe"
            else -> "java"
        }
        return getExecFolder().resolve("bin/$name").absolutePath
    }

}