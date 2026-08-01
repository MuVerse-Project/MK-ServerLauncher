package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import kotlin.random.Random

class MuServerLogPacket(
    targetServer: MCJEServer,
    val lvl: LogLevel,
    val msg: String,
    cid: Long = Random.nextCallId(),
): MuServerPacket(object: MuServerPacketInfo("console.log") {
    override fun fromData(data: JsonObject, cid: Long): MuServerLogPacket =
        throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}, targetServer, cid) {
    override fun getMSPData(): JsonObject = JsonObject().apply {
        addProperty("lvl", lvl.name)
        addProperty("msg", msg)
    }

    enum class LogLevel{
        DEBUG, INFO, WARN, ERROR
    }
}