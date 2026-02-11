package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * # MuPacket Reader
 *
 * Lightweight helper to decode MuPacket payloads from either JSON text
 * or already parsed [JsonObject] instances.
 *
 * @since TinyNova V1 | DEV.2
 * @author Mu_Cloud
 */
object MuPacketReader {

    /**
     * Parse JSON text and convert it to a [MuPacket].
     */
    fun toPacket(rawText: String): MuPacket {
        val parsed = try {
            JsonParser.parseString(rawText).asJsonObject
        } catch (e: Exception) {
            throw UnsupportedOperationException("Received raw MuPacket text is not a valid JsonObject", e)
        }
        return toPacket(parsed)
    }

    /**
     * Convert JsonObject payload to a [MuPacket].
     */
    fun toPacket(raw: JsonObject): MuPacket = MuPacketFactory.toPacket(raw)
}
