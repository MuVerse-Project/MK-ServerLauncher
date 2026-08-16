package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

val muServerListPacketInfo = object : MuPacketInfo<MuServerListPacket> {
    override val pid: String = "mucore.muserver:list"
    override fun fromData(data: JsonObject, cid: Long): MuServerListPacket = throw UnsupportedOperationException("Do not send MuServerListPacket to MuCore!")
}

val muServerInfoPacketInfo = object : MuServerPacketInfo("info") {
    override fun fromData(data: JsonObject, cid: Long): MuServerInfoPacket =
        throw UnsupportedOperationException("MuServerInfoPacket not supported send to MuPacketReceiver, it should be send to MuView")
}

val muServerLogPacketInfo = object: MuServerPacketInfo("console.log") {
    override fun fromData(data: JsonObject, cid: Long): MuServerLogPacket =
        throw UnsupportedOperationException("MuServerLogPacket not supported send to MuPacketReceiver, it should be send to MuView")
}

val muServerStatusPacketInfo = object : MuServerPacketInfo("status") {
    override fun fromData(data: JsonObject, cid: Long): MuServerStatusPacket =
        throw UnsupportedOperationException("MuServerStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
}