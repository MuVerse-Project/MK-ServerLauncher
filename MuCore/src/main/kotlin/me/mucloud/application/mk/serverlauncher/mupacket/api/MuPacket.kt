package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * # MuPacket API
 *
 * The Core Concept of MuPacketAPI
 *
 * @suppress Unstable API, and may be changed in the future. Do not implement directly! Please extend [AbstractMuPacket]
 * @see AbstractMuPacket
 * @since RainyZone V1 | DEV.1
 * @author Mu_Cloud
 */
interface MuPacket {

    /**
     * ### MuPacket ID
     *
     * Define the MP_ID value in [JsonObject] from [toJson]
     *
     * @return The MuPacket ID
     * @see toJson
     * @since RainyZone V1 | DEV.1
     */
    fun getInfo(): MuPacketInfo<*>

    /**
     * ### MuPacket Customized Data
     *
     * Define the MP_DATA value in [JsonObject] from [toJson]
     *
     * @return The Serialized MuPacket Custom Data as [JsonObject]
     * @see toJson
     * @since RainyZone V1 | DEV.1
     */
    fun getData(): JsonElement

    /**
     * ### MuPacket2JSON
     *
     * Basic Structure
     *
     * MP_ID: MuPacket ID
     *
     * MP_DATA: Customized MuPacket from [getData]
     *
     * @return The Serialized MuPacket as [JsonObject]
     * @see getData
     * @since RainyZone V1 | DEV.1
     */
    fun toJson(): JsonObject

    /**
     * ### MuPacket Call ID
     *
     * @return The Call ID of MuPacket
     * @since RainyZone V1 | DEV.1
     */
    fun getCallId(): Long

}