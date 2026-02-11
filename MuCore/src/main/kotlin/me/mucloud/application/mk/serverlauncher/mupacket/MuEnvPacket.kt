package me.mucloud.application.mk.serverlauncher.mupacket

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket

abstract class MuEnvPacket(
    val targetJEnv: JavaEnvironment,
): AbstractMuPacket() {

    final override val id: String = "muenv"

    fun getEnv(): JavaEnvironment = targetJEnv

    final override fun getData(): JsonObject = JsonObject().apply {
        addProperty("EV_NAME", targetJEnv.name)
        add("EV_OP", getMEPData())
    }

    abstract fun getMEPData(): JsonObject
    abstract override fun operate(): Boolean

}