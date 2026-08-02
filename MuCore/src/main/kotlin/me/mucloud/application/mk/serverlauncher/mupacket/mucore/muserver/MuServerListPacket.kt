package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import kotlin.random.Random

class MuServerListPacket(
    serverList: List<MCJEServer>,
    cid: Long = Random.nextCallId(),
): MuMsgPacket(muServerListPacketInfo, MuMsgStatus.INFO, gson.toJson(serverList), cid)