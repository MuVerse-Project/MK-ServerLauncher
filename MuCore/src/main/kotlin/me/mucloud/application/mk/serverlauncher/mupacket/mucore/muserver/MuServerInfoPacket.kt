package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool

class MuServerInfoPacket(
    ms: MCJEServer,
    tss: Long = System.currentTimeMillis(),
): MuMsgPacket(MuMsgPacketInfo("muserver.info"), MuMsgStatus.INFO, gson.toJson(ms), tss)