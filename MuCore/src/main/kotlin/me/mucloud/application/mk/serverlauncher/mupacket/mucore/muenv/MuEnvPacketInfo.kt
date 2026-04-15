package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo

abstract class MuEnvPacketInfo(
    typeID: String
): MuPacketInfo<MuEnvPacket> {
    override val pid: String = "mucore.muenv:$typeID"
}