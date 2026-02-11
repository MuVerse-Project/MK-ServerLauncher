package me.mucloud.application.mk.serverlauncher.mucore.external

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini

object MuUpdater {

    private const val DEFAULT_RELEASE_API = "https://api.github.com/repos/MuCloudOfficial/MK-ServerLauncher/releases/latest"

    /**
     * Fetch latest release metadata from GitHub.
     */
    fun getLatestRelease(url: String = DEFAULT_RELEASE_API): UpdateRelease {
        val json = MuHTTPClient.getJsonObject(url)
        return json.toUpdateRelease()
    }

    /**
     * Compare current core version and latest release.
     */
    fun checkForUpdate(currentVersion: String = MuCoreMini.getMuCoreInfo().ver): UpdateCheckResult {
        val latest = getLatestRelease()
        val hasUpdate = compareVersion(latest.version, currentVersion) > 0
        return UpdateCheckResult(currentVersion, latest, hasUpdate)
    }

    private fun JsonObject.toUpdateRelease(): UpdateRelease {
        val tagName = getAsJsonPrimitive("tag_name")?.asString.orEmpty()
        val displayName = getAsJsonPrimitive("name")?.asString.orEmpty().ifBlank { tagName }
        val body = getAsJsonPrimitive("body")?.asString.orEmpty()
        val publishedAt = getAsJsonPrimitive("published_at")?.asString.orEmpty()
        val htmlUrl = getAsJsonPrimitive("html_url")?.asString.orEmpty()

        return UpdateRelease(
            version = normalizeVersion(tagName).ifBlank { normalizeVersion(displayName) },
            tagName = tagName,
            name = displayName,
            body = body,
            publishedAt = publishedAt,
            url = htmlUrl
        )
    }

    private fun normalizeVersion(raw: String): String {
        return raw.trim().removePrefix("v").removePrefix("V")
    }

    /**
     * Semantic-ish version comparison with graceful fallback.
     * Numeric sections are compared in order; non-numeric suffixes are ignored.
     */
    internal fun compareVersion(left: String, right: String): Int {
        val l = parseVersion(normalizeVersion(left))
        val r = parseVersion(normalizeVersion(right))

        val maxSize = maxOf(l.size, r.size)
        repeat(maxSize) { index ->
            val lv = l.getOrElse(index) { 0 }
            val rv = r.getOrElse(index) { 0 }
            if (lv != rv) return lv.compareTo(rv)
        }

        return 0
    }

    private fun parseVersion(version: String): List<Int> {
        if (version.isBlank()) return emptyList()

        return version
            .split('.')
            .mapNotNull { token ->
                val numeric = token.takeWhile { it.isDigit() }
                numeric.toIntOrNull()
            }
    }
}

data class UpdateRelease(
    val version: String,
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val url: String,
)

data class UpdateCheckResult(
    val currentVersion: String,
    val latestRelease: UpdateRelease,
    val hasUpdate: Boolean,
)