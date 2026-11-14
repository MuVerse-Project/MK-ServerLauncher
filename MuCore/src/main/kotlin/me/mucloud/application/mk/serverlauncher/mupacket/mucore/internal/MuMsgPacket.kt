package me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket2View
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo
import kotlin.random.Random

object MuMsgPacketInfo: MuPacketInfo<MuMsgPacket>, MuPacket2View {
    override val pid: String = "mucore.internal:msg"
    override fun fromData(data: JsonObject, cid: Long): MuMsgPacket = throw UnsupportedOperationException("Do not send MuMsgPacket to MuCore!")
}

open class MuMsgPacket(
    val status: MuMsgStatus,
    val msg: String,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(MuMsgPacketInfo, cid), MuPacket2View {
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("type", status.name)
        addProperty("msg", msg)
    }
}

class MuMsgInfoPacket(msg: String, cid: Long = Random.nextCallId()): MuMsgPacket(MuMsgStatus.INFO, msg, cid)
class MuMsgWarnPacket(msg: String, cid: Long = Random.nextCallId()): MuMsgPacket(MuMsgStatus.WARN, msg, cid)
class MuMsgErrPacket(msg: String, cid: Long = Random.nextCallId()): MuMsgPacket(MuMsgStatus.ERR, msg, cid)
class MuMsgOKPacket(msg: String, cid: Long = Random.nextCallId()): MuMsgPacket(MuMsgStatus.OK, msg, cid)
class MuMsgTipPacket(msg: String, cid: Long = Random.nextCallId()): MuMsgPacket(MuMsgStatus.TIP, msg, cid)

enum class MuMsgStatus {
    INFO, OK, WARN, ERR, TIP
}