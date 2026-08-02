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
): MuServerPacket(muServerLogPacketInfo, targetServer, cid) {
    override fun getMSPData(): JsonObject = JsonObject().apply {
        addProperty("lvl", lvl.name)
        addProperty("msg", msg)
    }

    enum class LogLevel{
        DEBUG, INFO, WARN, ERROR
    }
}