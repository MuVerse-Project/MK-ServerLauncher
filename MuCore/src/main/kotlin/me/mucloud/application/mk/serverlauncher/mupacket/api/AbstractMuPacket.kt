package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

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
    protected val cid: Long = Random.nextCallId(),
) : MuPacket {

    final override fun getInfo(): MuPacketInfo<*> = type

    final override fun toJson(): JsonObject = JsonObject().apply {
        addProperty("MP_ID", type.pid)
        add("MP_DATA", getData())
        addProperty("TSS", getCallId())
    }

    final override fun getCallId(): Long = cid

    abstract override fun getData(): JsonObject
}