package me.mucloud.application.mk.serverlauncher.mucore.external

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

object MuHTTPClient {

    private val instance = OkHttpClient()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * # | Utils
     *
     * ## Get Body Message by Specified URL
     *
     * @param url Request URL
     * @return A Request body as [JsonObject]
     */
    fun getJsonObject(url: String): JsonObject {
        val request = Request.Builder().url(url).build()
        instance.newCall(request).execute().use { i ->
            return gson.toJsonTree(i.body.string()).asJsonObject
        }
    }

    fun getJsonObject(url: String, key: String): JsonObject = getJsonObject(url).getAsJsonObject(key)


    /**
     * # | Utils
     *
     * ## Get Body Message by Specified URL
     *
     * @param url Request URL
     * @return A Request body as [JsonArray]
     */
    fun getJsonArray(url: String): JsonArray {
        val request = Request.Builder().url(url).build()
        instance.newCall(request).execute().use { i ->
            return gson.toJsonTree(i.body.string()).asJsonArray
        }
    }

    fun getJsonArray(url: String, key: String): JsonArray = getJsonObject(url).getAsJsonArray(key)

    /**
     * File Downloader
     *
     * @since TinyNova V0
     */
    fun downloadFile(url: String, file: File) {
        val request = Request.Builder().url(url).build()
        instance.newCall(request).execute().use { i ->
            i.body.byteStream().use { s ->
                if(!file.exists()){ file.createNewFile() }
                check(file.isFile){ "Target $file is not a file" }
                info("MuClient", "Downloading $url to ${file.name}")
                file.outputStream().use(s::copyTo)
                info("MuClient", "Downloaded $url to ${file.name}")
            }
        }
    }
}