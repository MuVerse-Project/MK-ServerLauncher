package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver

import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

abstract class MuServerPacketInfo(
    typeID: String
): MuPacketInfo<MuServerPacket> {
    override val pid: String = "mucore.muserver:$typeID"
}