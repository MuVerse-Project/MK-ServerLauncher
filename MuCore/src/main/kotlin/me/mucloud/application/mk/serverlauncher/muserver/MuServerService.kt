package me.mucloud.application.mk.serverlauncher.muserver

import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerInfoPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerListPacket

object MuServerService {

    fun getServerList(): MuPacket = MuServerListPacket(ServerPool.getMuServerList())

    fun getServerInfo(msid: String): MuPacket{
        val target = ServerPool.getMuServer(msid) ?: return MuMsgErrPacket("MuServer $msid not Found")
        return MuServerInfoPacket(target)
    }
}