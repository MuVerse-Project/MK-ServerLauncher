package me.mucloud.application.mk.serverlauncher.mupacket

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer

abstract class MuServerPacket(
    val targetServer: MCJEServer,
): AbstractMuPacket() {

    final override val id: String = "muserver"

    fun getServer() = targetServer

    final override fun getData(): JsonObject = JsonObject().apply{
        addProperty("SV_NAME", targetServer.getName())
        add("SV_OP", getMSPData())
    }

    abstract fun getMSPData(): JsonObject
    abstract override fun operate(): Boolean

}