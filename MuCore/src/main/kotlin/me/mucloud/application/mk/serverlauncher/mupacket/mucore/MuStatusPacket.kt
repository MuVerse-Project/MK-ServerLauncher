package me.mucloud.application.mk.serverlauncher.mupacket.mucore

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

class MuStatusPacket(val status: MuMsgStatus): AbstractMuPacket(
    object: MuPacketInfo<MuStatusPacket>{
        override val pid: String = "mucore.internal:status"
        override fun fromData(data: JsonObject): MuStatusPacket {
            val status = data.get("status").asString ?: error("Exception occurred while parsing MuPacket >> Invalid MuMsgStatus")
            return MuStatusPacket(MuMsgStatus.valueOf(status))
        }
    }
) {
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("status", status.name)
    }
}