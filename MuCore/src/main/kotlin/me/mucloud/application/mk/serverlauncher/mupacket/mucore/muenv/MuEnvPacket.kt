package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket

abstract class MuEnvPacket(
    val targetJEnv: JavaEnvironment,
    mepInfo: MuEnvPacketInfo,
) : AbstractMuPacket(mepInfo) {
    final override fun getData(): JsonObject = JsonObject().apply {
        addProperty("EV_NAME", targetJEnv.name)
        add("EV_OP", getMEPData())
    }

    abstract fun getMEPData(): JsonObject
}