package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject

/**
 * # MuPacket API
 *
 * ## MuPacket Info
 *
 * MuPacket PID & Deserializer Provider
 *
 * It should be Defined if you want to register the SubClasses of MuPacket
 *
 * @since RainyZone V1 | DEV.5
 * @author Mu_Cloud
 */
interface MuPacketInfo<out T: MuPacket> {

    /**
     * ### MuPacket PID (MP_ID)
     */
    val pid: String

    /**
     * ### MuPacket Deserializer
     *
     * The Base of MuPacket Data (MP_DATA) Deserializer
     *
     * @param data The MP_DATA Content as [JsonObject] in the MuPacket
     * @since RainyZone V1 | DEV.5
     */
    fun fromData(data: JsonObject, tss: Long): T
}
