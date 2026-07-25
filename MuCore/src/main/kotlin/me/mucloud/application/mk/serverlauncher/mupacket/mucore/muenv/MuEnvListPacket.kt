package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus

class MuEnvListPacket(
    envList: List<JavaEnvironment>,
    tss: Long = System.currentTimeMillis(),
): MuMsgPacket(MuMsgPacketInfo("muenv.list"), MuMsgStatus.INFO, gson.toJson(envList), tss)