package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * # MuPacket API
 *
 * The Core Concept of MuPacketAPI
 *
 * @since TinyNova V1 | DEV.2
 * @author Mu_Cloud
 */
interface MuPacket {

    fun operate(): Boolean

    /**
     * MuPacket Customized Data
     *
     * Define the MP_DATA value in [JsonObject] from [toJson]
     *
     * @return The Serialized MuPacket Custom Data as [JsonElement]
     * @see toJson
     * @since TinyNova V1 | DEV.1
     */
    fun getData(): JsonObject

    /**
     * MuPacket ID
     *
     * Define the MP_ID value in [JsonObject] from [toJson]
     *
     * @return The MuPacket ID
     * @see toJson
     * @since TinyNova V1 | DEV.1
     */
    fun getPID(): String

    /**
     * MuPacket2JSON
     *
     * Basic Structure
     *
     * MP_ID: MuPacket ID
     *
     * MP_DATA: Customized MuPacket from [getData]
     *
     * @return The Serialized MuPacket as [JsonObject]
     * @see getData
     * @since TinyNova V1 | DEV.1
     */
    fun toJson(): JsonObject

    /**
     * MuPacket Timestamp
     *
     * @return The created Time of MuPacket as Millis
     * @since TinyNova V1 | DEV.1
     */
    fun getTimestamp(): Long

    /**
     * MuPacket4Json: MP_DATA Converter
     *
     * @param mpdata The MP_DATA content from Json Data by [MuPacket.toJson]
     * @return A [MuPacket] that loaded the MP_DATA json data
     * @since TinyNova V1 | DEV.1
     */
    fun toPacket(mpdata: JsonObject): MuPacket?

}