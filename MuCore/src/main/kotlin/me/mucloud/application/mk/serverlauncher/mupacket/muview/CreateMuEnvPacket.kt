package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.muenv.MuEnvironmentService
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExcutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

class CreateMuEnvPacket(
    val ev: JavaEnvironment,
    cid: Long = Random.nextCallId()
): AbstractMuPacket(createMuEnvPacketInfo, cid = cid), ExcutableMuPacket {
    override fun getData(): JsonObject = JsonObject().apply {
        addProperty("name", ev.name)
        addProperty("path", ev.path)
    }

    override fun excute(): MuPacket = MuEnvironmentService.regMuEnvironment(ev)
}