package me.mucloud.application.mk.serverlauncher.muenv

import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgWarnPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.MuEnvInfoPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muenv.MuEnvListPacket

object MuEnvironmentService {

    fun getMuEnvList(): MuPacket = MuEnvListPacket(EnvPool.getEnvList())

    fun getMuEnv(evid: String): MuPacket{
        val callback = EnvPool.getEnv(evid)
        if(callback.isOk){
            return MuEnvInfoPacket(callback.value!!)
        }else{
            return MuMsgWarnPacket("MuEnv not found: ${callback.msg}")
        }
    }


    fun regMuEnvironment(ev: JavaEnvironment): MuPacket {
        val callback = EnvPool.regEnv(ev)
        if(callback.isOk){
            return MuMsgOKPacket("Java Environment Registered")
        }else{
            return MuMsgErrPacket("Java Environment Register Failed: ${callback.msg}")
        }

    }

    fun delMuEnvironment(evid: String): MuPacket {
        val callback = EnvPool.delEnv(evid)
        return if(callback.isOk){
            MuMsgOKPacket("Java Environment Deleted")
        }else{
            MuMsgErrPacket("Java Environment Delete Failed: ${callback.msg}")
        }
    }
}