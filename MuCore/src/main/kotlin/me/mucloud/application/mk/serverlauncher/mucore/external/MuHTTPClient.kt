package me.mucloud.application.mk.serverlauncher.mucore.external

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request

object MuHTTPClient {

    private val instance = OkHttpClient()

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
        instance.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Request failed with HTTP ${response.code}: $url")
            }
            val body = response.body?.string()?.trim().orEmpty()
            if (body.isEmpty()) {
                throw IllegalStateException("Empty response body: $url")
            }
            val jsonElement = JsonParser.parseString(body)
            if (!jsonElement.isJsonObject) {
                throw IllegalStateException("Response is not a JSON object: $url")
            }
            return jsonElement.asJsonObject
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
        instance.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Request failed with HTTP ${response.code}: $url")
            }
            val body = response.body?.string()?.trim().orEmpty()
            if (body.isEmpty()) {
                throw IllegalStateException("Empty response body: $url")
            }
            val jsonElement = JsonParser.parseString(body)
            if (!jsonElement.isJsonArray) {
                throw IllegalStateException("Response is not a JSON array: $url")
            }
            return jsonElement.asJsonArray
        }
    }

    fun getJsonArray(url: String, key: String): JsonArray = getJsonObject(url).getAsJsonArray(key)

}