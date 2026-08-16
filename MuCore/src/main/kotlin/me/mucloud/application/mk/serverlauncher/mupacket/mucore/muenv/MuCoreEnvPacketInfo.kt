package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

val muEnvListPacketInfo = object: MuPacketInfo<MuEnvListPacket>{
    override val pid: String = "mucore.muenv:list"
    override fun fromData(data: JsonObject, cid: Long): MuEnvListPacket =
        throw UnsupportedOperationException("MuEnvListPacket not supported send to MuPacketReceiver, it should be send to MuView")
}

val muEnvInfoPacketInfo = object: MuEnvPacketInfo("info"){
    override fun fromData(data: JsonObject, cid: Long): MuEnvPacket =
        throw UnsupportedOperationException("MuEnvInfoPacket not supported send to MuPacketReceiver, it should be send to MuView")
}