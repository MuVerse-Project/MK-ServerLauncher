package me.mucloud.application.mk.serverlauncher.mupacket.mucore

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

open class MuMsgPacket(
    mspinfo: MuMsgPacketInfo,
    val status: MuMsgStatus,
    val msg: String,
    tss: Long = System.currentTimeMillis(),
): AbstractMuPacket(mspinfo, tss){
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("status", status.name)
        addProperty("msg", msg)
    }
}

class MuMsgInfoPacket(msg: String): MuMsgPacket(MuMsgPacketInfo("info"), MuMsgStatus.INFO, msg)

class MuMsgWarnPacket(msg: String): MuMsgPacket(MuMsgPacketInfo("warn"), MuMsgStatus.WARN, msg)

class MuMsgErrPacket(msg: String): MuMsgPacket(MuMsgPacketInfo("err"), MuMsgStatus.ERR, msg)

class MuMsgOKPacket(msg: String): MuMsgPacket(MuMsgPacketInfo("ok"), MuMsgStatus.OK, msg)

class MuMsgTipPacket(msg: String): MuMsgPacket(MuMsgPacketInfo("tip"), MuMsgStatus.TIP, msg)