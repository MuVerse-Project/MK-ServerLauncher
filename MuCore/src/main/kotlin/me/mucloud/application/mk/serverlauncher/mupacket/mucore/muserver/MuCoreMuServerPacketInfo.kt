package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo

val muServerInfoPacketInfo = MuMsgPacketInfo("muserver.info")

val muServerListPacketInfo = MuMsgPacketInfo("muserver.list")

val muServerLogPacketInfo = object: MuServerPacketInfo("console.log") {
    override fun fromData(data: JsonObject, cid: Long): MuServerLogPacket =
        throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}

val muServerStatusPacketInfo = object : MuServerPacketInfo("status") {
    override fun fromData(data: JsonObject, cid: Long): MuServerStatusPacket =
        throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}