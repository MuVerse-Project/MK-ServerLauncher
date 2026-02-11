package me.mucloud.application.mk.serverlauncher.common.env

import java.io.File
import java.io.FileReader
import java.util.Properties

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
    val path: String,
){

    private val distributionVer: String

    init{
        val map = getReleaseFileContent()
        distributionVer = "${map["JAVA_VERSION"] ?: "Unknown"}(${map["IMPLEMENTOR_VERSION"] ?: "Unknown"})"
    }

    /**
     * Get the Content of File named "RELEASE" in the Java Installation Folder
     *
     * @return The File Content as Map
     */
    private fun getReleaseFileContent(): Map<String, String>{
        val releaseFile = File(path).resolve("release")
        if(!releaseFile.exists()) return emptyMap()

        return FileReader(releaseFile).use { reader ->
            Properties().apply { load(reader) }
                .entries
                .associate { (k, v) ->
                    k.toString().trim() to v.toString().trim().removeSurrounding("\"")
                }
        }
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
    fun getVersion(): JavaVersion = JavaVersion.get(this)


    /**
     * Get the Executable File
     *
     * @return The executable file of JavaEnvironment, which usually refers to the "java.exe" file
     */
    fun getExecFolder(): File = File(path)

    fun getAbsoluteExecPath(): String = getExecFolder().resolve("bin/java.exe").absolutePath
}