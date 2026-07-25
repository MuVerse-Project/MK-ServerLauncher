package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus

class MuEnvInfoPacket(
    ev: JavaEnvironment,
    tss: Long = System.currentTimeMillis(),
): MuMsgPacket(MuMsgPacketInfo("muenv.info"), MuMsgStatus.INFO, gson.toJson(ev), tss)