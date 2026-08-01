package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import kotlin.random.Random

abstract class MuServerPacket(
    mspInfo: MuServerPacketInfo,
    val targetServer: MCJEServer,
    cid: Long = Random.nextCallId(),
) : AbstractMuPacket(mspInfo, cid) {
    final override fun getData(): JsonObject = JsonObject().apply{
        addProperty("MS_ID", targetServer.msi.msid)
        add("MS_OP", getMSPData())
    }

    abstract fun getMSPData(): JsonObject
}