package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import me.mucloud.application.mk.serverlauncher.muserver.MuServerService
import kotlin.random.Random

class CreateMuServerPacket(
    val ms: MCJEServer,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(createMuServerPacketInfo, cid), ExecutableMuPacket {
    override fun getData(): JsonObject = JsonObject().apply {
        add("MSI", gson.toJsonTree(ms.msi))
        add("MSSC", gson.toJsonTree(ms.mssc))
    }

    override fun execute(): MuPacket = MuServerService.createMuServer(ms)
}