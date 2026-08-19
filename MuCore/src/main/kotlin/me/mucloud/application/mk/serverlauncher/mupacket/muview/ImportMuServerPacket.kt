package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.ExecutableMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import java.io.File
import kotlin.random.Random

class ImportMuServerPacket(
    val loc: File,
    val mssc: MCJEServer.StartupConfig,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(importMuServerPacketInfo, cid), ExecutableMuPacket {
    override fun getData(): JsonObject = JsonObject().apply {
        add("LOC", gson.toJsonTree(loc))
        add("MSSC", gson.toJsonTree(mssc))
    }

    override fun execute(): MuPacket = TODO()
}