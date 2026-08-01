package me.mucloud.application.mk.serverlauncher.muserver

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.MuMsgWarnPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerInfoPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerListPacket

object MuServerService {

    fun getServerList(): MuPacket = MuServerListPacket(ServerPool.getMuServerList())

    fun getServerInfo(msid: String): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            MuServerInfoPacket(callback.value!!)
        }else{
            MuMsgWarnPacket("Server info could not be registered: ${callback.msg}")
        }
    }

    fun getAvaliableServerTypes(): MuPacket{
        val target = ServerPool.getMuServerList()
        return MuMsgOKPacket(gson.toJson(target))
    }

    fun createMuServer(ms: MCJEServer): MuPacket{
        val callback = ServerPool.regMuServer(ms)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Created")
        }else{
            MuMsgErrPacket("MuServer Create Failed: ${callback.msg}")
        }
    }

    fun importMuServer(ms: MCJEServer): MuPacket{
        val callback = ServerPool.importMuServer(ms)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Imported")
        }else{
            MuMsgErrPacket("MuServer Import Failed: ${callback.msg}")
        }
    }

    fun deleteMuServer(msid: String): MuPacket{
        val callback = ServerPool.delMuServer(msid)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Deleted")
        }else{
            MuMsgErrPacket("MuServer Delete Failed: ${callback.msg}")
        }

    }

    fun removeMuServer(msid: String): MuPacket{
        val callback = ServerPool.removeMuServer(msid)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Removed")
        }else{
            MuMsgErrPacket("MuServer Remove Failed: ${callback.msg}")
        }
    }

    // TODO: Unstable in ServerPool & MCJEServer
    fun startMuServer(msid: String): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            callback.value!!.startMuServer()
            MuMsgOKPacket("MuServer Started")
        }else{
            MuMsgErrPacket("MuServer Start Failed: ${callback.msg}")
        }
    }

    // TODO: Unstable in ServerPool & MCJEServer
    fun stopMuServer(msid: String, enforce: Boolean): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            callback.value!!.stopMuServer(enforce)
            MuMsgOKPacket("MuServer Started")
        }else{
            MuMsgErrPacket("MuServer Start Failed: ${callback.msg}")
        }
    }
}