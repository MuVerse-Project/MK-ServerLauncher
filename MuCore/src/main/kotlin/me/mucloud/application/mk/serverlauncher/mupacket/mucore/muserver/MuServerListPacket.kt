package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonArray
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import kotlin.random.Random

class MuServerListPacket(
    val serverList: List<MCJEServer>,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(muServerListPacketInfo, cid){
    override fun getData(): JsonArray = gson.toJsonTree(serverList) as JsonArray
}