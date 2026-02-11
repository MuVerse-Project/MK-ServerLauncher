package me.mucloud.application.mk.serverlauncher.muserver

import java.io.File
import java.nio.file.Path

/**
 *  MuAPI | MCJEServer Type
 *
 *  - The Minecraft Server Type Marker, used to create, download and marked Minecraft Server in MuView.
 *  - Server Information Provider, including Download API, Available Version List API and Setting Files Structure.
 *
 * @since TinyNova V1 | DEV.1
 * @author Mu_Cloud
 */
abstract class MCJEServerType(
    val id: String,
    val isModded: Boolean,
    val name: String,
    val desc: String,
) {
    abstract fun getAvailableVersions(): List<String>
    abstract fun getServerCore(vercode: String): File
    abstract fun getServerCoreSettingsFile(): List<Path>
}