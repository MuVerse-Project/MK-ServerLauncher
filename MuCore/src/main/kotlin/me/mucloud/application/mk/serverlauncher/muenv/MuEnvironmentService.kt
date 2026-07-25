package me.mucloud.application.mk.serverlauncher.muenv

import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.MuEnvInfoPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.MuEnvListPacket

object MuEnvironmentService {

    fun getMuEnvList(): MuPacket = MuEnvListPacket(EnvPool.getEnvList())

    fun getMuEnv(ev: String): MuPacket =
        MuEnvInfoPacket(EnvPool.getEnv(ev) ?: return MuMsgErrPacket("MuEnvironment $ev not Found"))

    fun regMuEnvironment(ev: JavaEnvironment): MuPacket {
        EnvPool.regEnv(ev)
        return MuMsgOKPacket("Java Environment Registered")
    }
}