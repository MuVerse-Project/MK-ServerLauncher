package me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo
import kotlin.random.Random

object MuMsgPacketInfo: MuPacketInfo<MuMsgPacket> {
    override val pid: String = "mucore.internal:msg"
    override fun fromData(data: JsonObject, cid: Long): MuMsgPacket = throw UnsupportedOperationException("Do not send MuMsgPacket to MuCore!")
}

open class MuMsgPacket(
    val status: MuMsgStatus,
    val msg: String,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(MuMsgPacketInfo, cid){
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("status", status.name)
        addProperty("msg", msg)
    }
}

class MuMsgInfoPacket(msg: String): MuMsgPacket(MuMsgStatus.INFO, msg)
class MuMsgWarnPacket(msg: String): MuMsgPacket(MuMsgStatus.WARN, msg)
class MuMsgErrPacket(msg: String): MuMsgPacket(MuMsgStatus.ERR, msg)
class MuMsgOKPacket(msg: String): MuMsgPacket(MuMsgStatus.OK, msg)
class MuMsgTipPacket(msg: String): MuMsgPacket(MuMsgStatus.TIP, msg)

enum class MuMsgStatus {
    INFO, OK, WARN, ERR, TIP
}