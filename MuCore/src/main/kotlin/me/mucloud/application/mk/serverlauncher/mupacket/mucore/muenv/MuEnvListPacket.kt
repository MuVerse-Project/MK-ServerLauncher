package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

class MuEnvListPacket(
    val envList: List<JavaEnvironment>,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(muEnvListPacketInfo, cid){
    override fun getData(): JsonObject = gson.toJsonTree(envList) as JsonObject
}