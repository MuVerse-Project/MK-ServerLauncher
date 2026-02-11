package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject

/**
 * # MuPacket API
 *
 * The Core Concept of MuPacketAPI
 *
 * @since TinyNova V1 | DEV.2
 * @author Mu_Cloud
 */
abstract class AbstractMuPacket: MuPacket {

    abstract val id: String
    abstract val operation: String


    final override fun getPID(): String = "mucore:$id:$operation"

    final override fun toJson(): JsonObject = JsonObject().apply{
        addProperty("MP_ID", getPID())
        add("MP_DATA", getData())
    }

    final override fun getTimestamp(): Long = System.currentTimeMillis()

    abstract override fun getData(): JsonObject

    abstract override fun operate(): Boolean
}