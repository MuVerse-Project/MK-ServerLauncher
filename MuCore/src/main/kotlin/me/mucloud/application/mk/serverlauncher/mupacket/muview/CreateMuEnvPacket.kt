package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.muenv.MuEnvironmentService
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

class CreateMuEnvPacket(
    val evName: String,
    val evPath: String,
    cid: Long = Random.nextCallId()
): AbstractMuPacket(createMuEnvPacketInfo, cid = cid), ExecutableMuPacket {
    override fun getData(): JsonObject = JsonObject().apply {
        addProperty("name", evName)
        addProperty("path", evPath)
    }

    override fun execute(): MuPacket = MuEnvironmentService.regMuEnvironment(evName, evPath)
}