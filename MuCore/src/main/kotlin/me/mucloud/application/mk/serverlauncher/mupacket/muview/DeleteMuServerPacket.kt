package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import kotlin.random.Random

class DeleteMuServerPacket(
    val msid: String,
    cid: Long = Random.nextCallId(),
): AbstractMuPacket(deleteMuServerPacketInfo, cid) {
    override fun getData(): JsonObject = JsonObject().apply{
        addProperty("msid", msid)
    }
}