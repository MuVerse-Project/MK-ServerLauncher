package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer

class MuServerListPacket(
    serverList: List<MCJEServer>,
    tss: Long = System.currentTimeMillis(),
): MuMsgPacket(MuMsgPacketInfo("muserver.list"), MuMsgStatus.INFO, gson.toJson(serverList), tss) {
}