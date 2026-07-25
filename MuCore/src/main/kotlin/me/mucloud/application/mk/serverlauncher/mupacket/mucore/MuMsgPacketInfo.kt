package me.mucloud.application.mk.serverlauncher.mupacket.mucore

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

open class MuMsgPacketInfo(
    typeID: String
): MuPacketInfo<MuMsgPacket> {
    override val pid: String = "mucore.msg:$typeID"
    override fun fromData(data: JsonObject, tss: Long): MuMsgPacket = throw UnsupportedOperationException("Do not send MuMsgPacket to MuCore!")
}