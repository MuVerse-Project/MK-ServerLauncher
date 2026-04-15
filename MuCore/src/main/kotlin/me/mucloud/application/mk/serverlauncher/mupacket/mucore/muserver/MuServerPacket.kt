package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer

abstract class MuServerPacket(
    mspInfo: MuServerPacketInfo,
    val targetServer: MCJEServer,
) : AbstractMuPacket(mspInfo) {
    final override fun getData(): JsonObject = JsonObject().apply{
        addProperty("MS_ID", targetServer.msi.MSID)
        add("MS_OP", getMSPData())
    }

    abstract fun getMSPData(): JsonObject
}