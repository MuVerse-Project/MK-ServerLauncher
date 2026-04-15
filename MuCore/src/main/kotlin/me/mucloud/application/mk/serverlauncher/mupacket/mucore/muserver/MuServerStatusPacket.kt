package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer

class MuServerStatusPacket(
    targetServer: MCJEServer,
): MuServerPacket(object : MuServerPacketInfo("status") {
    override fun fromData(data: JsonObject): MuServerStatusPacket = throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}, targetServer) {
    override fun getMSPData(): JsonObject = JsonObject().apply {
        addProperty("MSS", targetServer.mss.code)
    }
}