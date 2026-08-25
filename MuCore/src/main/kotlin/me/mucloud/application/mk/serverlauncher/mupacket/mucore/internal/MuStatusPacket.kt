package me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mucore.external.StatusPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.AbstractMuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo
import kotlin.random.Random

val muStatusPacketInfo = object: MuPacketInfo<MuStatusPacket>{
    override val pid: String = "mucore.internal:status"

    override fun fromData(
        data: JsonObject,
        cid: Long
    ): MuStatusPacket {
        throw UnsupportedOperationException("MuStatusPacket not supported send to MuPacketReceiver, it should be send to MuView")
    }

}

class MuStatusPacket(
    val status: StatusPacket,
    cid: Long = Random.nextCallId()
): AbstractMuPacket(muStatusPacketInfo, cid) {
    override fun getData(): JsonObject = gson.toJsonTree(status) as JsonObject
}