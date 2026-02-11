package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.net.URL
import java.nio.file.Path

object StandardMCJEServerTypes {

    private const val MOJANG_VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    private const val PAPER_API = "https://api.papermc.io/v2/projects"
    private const val LEAVES_API = "https://api.leavesmc.org/v2/projects"

    private fun readJsonObject(url: String): JsonObject {
        val text = URL(url).readText()
        return JsonParser.parseString(text).asJsonObject
    }

    private fun downloadToTemp(url: String, prefix: String): File {
        val dir = File(System.getProperty("java.io.tmpdir"), "mk-serverlauncher/core-cache")
        if (!dir.exists()) dir.mkdirs()
        val target = File(dir, "$prefix-core.jar")
        URL(url).openStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }

    private fun resolveMojangVersionMeta(version: String): String {
        val manifest = readJsonObject(MOJANG_VERSION_MANIFEST)
        val versions = manifest.getAsJsonArray("versions")
        val match = versions.firstOrNull { it.asJsonObject["id"].asString == version }
            ?: throw IllegalArgumentException("Version not found: $version")
        return match.asJsonObject["url"].asString
    }

    private fun getMojangAvailableVersions(): List<String> {
        val manifest = readJsonObject(MOJANG_VERSION_MANIFEST)
        return manifest.getAsJsonArray("versions")
            .map { it.asJsonObject["id"].asString }
    }

    private fun getPaperAvailableVersions(project: String): List<String> {
        val info = readJsonObject("$PAPER_API/$project")
        return info.getAsJsonArray("versions")
            .filter { it.asJsonObject["type"].asString == "release" } // Filter: Only Release
            .map { it.asJsonObject["id"].asString }
    }

    private fun getPaperLatestBuild(project: String, version: String): Int {
        val info = readJsonObject("$PAPER_API/$project/versions/$version")
        val builds = info.getAsJsonArray("builds")
        return builds.last().asInt
    }

    private fun getLeavesAvailableVersions(): List<String> {
        val info = readJsonObject("$LEAVES_API/leaves")
        return info.getAsJsonArray("versions").map { it.asString }
    }

    private fun getLeavesLatestBuild(version: String): Int {
        val info = readJsonObject("$LEAVES_API/leaves/versions/$version")
        val builds = info.getAsJsonArray("builds")
        return builds.last().asInt
    }

    private fun unstableSettings(): List<Path> {
        // NOTE: Settings files vary by core and are not stable yet.
        return emptyList()
    }

    val VANILLA = object : MCJEServerType("vanilla", false, "Vanilla", "Vanilla Server Core") {
        override fun getAvailableVersions(): List<String> = getMojangAvailableVersions()

        override fun getServerCore(vercode: String): File {
            val versionMetaUrl = resolveMojangVersionMeta(vercode)
            val versionMeta = readJsonObject(versionMetaUrl)
            val serverUrl = versionMeta.getAsJsonObject("downloads")
                .getAsJsonObject("server")["url"].asString
            return downloadToTemp(serverUrl, "vanilla-$vercode")
        }

        override fun getServerCoreSettingsFile(): List<Path> = unstableSettings()
    }

    val SPIGOT = object : MCJEServerType("spigot", false, "Spigot", "A Common and widely used Server Code") {
        override fun getAvailableVersions(): List<String> = getMojangAvailableVersions()

        override fun getServerCore(vercode: String): File {
            val url = "https://download.getbukkit.org/spigot/spigot-$vercode.jar"
            return downloadToTemp(url, "spigot-$vercode")
        }

        override fun getServerCoreSettingsFile(): List<Path> = unstableSettings()
    }

    val PAPER = object : MCJEServerType("paper", false, "PaperSpigot", "A High-Performance Server based on Spigot") {
        override fun getAvailableVersions(): List<String> = getPaperAvailableVersions("paper")

        override fun getServerCore(vercode: String): File {
            val build = getPaperLatestBuild("paper", vercode)
            val url = "$PAPER_API/paper/versions/$vercode/builds/$build/downloads/paper-$vercode-$build.jar"
            return downloadToTemp(url, "paper-$vercode-$build")
        }

        override fun getServerCoreSettingsFile(): List<Path> = unstableSettings()
    }

    val LEAVES = object : MCJEServerType("leaves", false, "Leaves", "Leaves") {
        override fun getAvailableVersions(): List<String> = getLeavesAvailableVersions()

        override fun getServerCore(vercode: String): File {
            val build = getLeavesLatestBuild(vercode)
            val url = "$LEAVES_API/leaves/versions/$vercode/builds/$build/downloads/leaves-$vercode-$build.jar"
            return downloadToTemp(url, "leaves-$vercode-$build")
        }

        override fun getServerCoreSettingsFile(): List<Path> = unstableSettings()
    }

    val FOLIA = object : MCJEServerType("folia", false, "Folia", "A High-Performance and Multi-Thread featured Server Code") {
        override fun getAvailableVersions(): List<String> = getPaperAvailableVersions("folia")

        override fun getServerCore(vercode: String): File {
            val build = getPaperLatestBuild("folia", vercode)
            val url = "$PAPER_API/folia/versions/$vercode/builds/$build/downloads/folia-$vercode-$build.jar"
            return downloadToTemp(url, "folia-$vercode-$build")
        }

        override fun getServerCoreSettingsFile(): List<Path> = unstableSettings()
    }

    val UNKNOWN = object : MCJEServerType("unknown", false, "Unknown", "Unknown") {
        override fun getAvailableVersions(): List<String> = emptyList()
        override fun getServerCore(vercode: String): File { throw UnsupportedOperationException("UNKNOWN SERVER TYPE IS NOT SUPPORTED!") }
        override fun getServerCoreSettingsFile(): List<Path> = emptyList()
    }
}
