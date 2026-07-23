package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject

/**
 * # MuPacket API
 *
 * Abstract MuPacket
 *
 * Main Parent of MuPacket API
 *
 * It should be extended which you want to create MuPacket's SubClass.
 *
 * @since TinyNova V1 | DEV.2
 * @author Mu_Cloud
 */
abstract class AbstractMuPacket(
    protected val type: MuPacketInfo<*>,
    protected val tss: Long = System.currentTimeMillis(),
) : MuPacket {

    final override fun getInfo(): MuPacketInfo<*> = type

    final override fun toJson(): JsonObject = JsonObject().apply {
        addProperty("MP_ID", type.pid)
        add("MP_DATA", getData())
        addProperty("TSS", getTimestamp())
    }

    final override fun getTimestamp(): Long = tss

    abstract override fun getData(): JsonObject
}