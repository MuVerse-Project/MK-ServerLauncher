package me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgPacketInfo
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgStatus
import kotlin.random.Random

class MuEnvInfoPacket(
    ev: JavaEnvironment,
    cid: Long = Random.nextCallId(),
): MuMsgPacket(MuMsgPacketInfo("muenv.info"), MuMsgStatus.INFO, gson.toJson(ev), cid)