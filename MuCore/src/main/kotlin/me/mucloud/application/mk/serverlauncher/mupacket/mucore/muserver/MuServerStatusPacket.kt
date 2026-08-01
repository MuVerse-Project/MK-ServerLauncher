package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.ServerStatus
import kotlin.random.Random

class MuServerStatusPacket(
    targetServer: MCJEServer,
    val newStatus: ServerStatus,
    cid: Long = Random.nextCallId(),
): MuServerPacket(object : MuServerPacketInfo("status") {
    override fun fromData(data: JsonObject, cid: Long): MuServerStatusPacket =
        throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}, targetServer, cid) {
    override fun getMSPData(): JsonObject = JsonObject().apply {
        addProperty("MSS", newStatus.code)
    }
}