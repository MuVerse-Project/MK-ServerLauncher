package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import kotlin.random.Random

class MuServerInfoPacket(
    val ms: MCJEServer,
    cid: Long = Random.nextCallId(),
): MuServerPacket(muServerInfoPacketInfo, ms, cid){
    override fun getMSPData(): JsonObject = gson.toJsonTree(ms) as JsonObject
}