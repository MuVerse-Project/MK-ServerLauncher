package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

abstract class MuEnvPacket(
    mepInfo: MuEnvPacketInfo,
    val targetJEnv: JavaEnvironment,
    cid: Long = Random.nextCallId(),
) : AbstractMuPacket(mepInfo, cid) {
    final override fun getData(): JsonObject = JsonObject().apply {
        addProperty("EV_NAME", targetJEnv.name)
        add("EV_OP", getMEPData())
    }

    abstract fun getMEPData(): JsonObject
}