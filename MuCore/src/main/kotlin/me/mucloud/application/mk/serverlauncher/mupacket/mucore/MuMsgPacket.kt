package me.mucloud.application.mk.serverlauncher.mupacket.mucore

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

open class MuMsgPacket(
    mspinfo: MuMsgPacketInfo,
    val status: MuMsgStatus,
    val msg: String,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(mspinfo, cid){
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("status", status.name)
        addProperty("msg", msg)
    }
}

val muMsgInfoPacketInfo = MuMsgPacketInfo("info")
val muMsgWarnPacketInfo = MuMsgPacketInfo("warn")
val muMsgErrPacketInfo = MuMsgPacketInfo("err")
val muMsgOKPacketInfo = MuMsgPacketInfo("ok")
val muMsgTipPacketInfo = MuMsgPacketInfo("tip")

class MuMsgInfoPacket(msg: String): MuMsgPacket(muMsgInfoPacketInfo, MuMsgStatus.INFO, msg)
class MuMsgWarnPacket(msg: String): MuMsgPacket(muMsgWarnPacketInfo, MuMsgStatus.WARN, msg)
class MuMsgErrPacket(msg: String): MuMsgPacket(muMsgErrPacketInfo, MuMsgStatus.ERR, msg)
class MuMsgOKPacket(msg: String): MuMsgPacket(muMsgOKPacketInfo, MuMsgStatus.OK, msg)
class MuMsgTipPacket(msg: String): MuMsgPacket(muMsgTipPacketInfo, MuMsgStatus.TIP, msg)