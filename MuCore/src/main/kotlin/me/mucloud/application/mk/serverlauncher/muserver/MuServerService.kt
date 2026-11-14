package me.mucloud.application.mk.serverlauncher.muserver

import me.mucloud.application.mk.serverlauncher.MuCoreMini.gson
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgOKPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgWarnPacket
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerInfoPacket
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.nextCallId
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.muserver.MuServerListPacket
import kotlin.random.Random

object MuServerService {

    fun getServerList(): MuPacket = MuServerListPacket(ServerPool.getMuServerList())

    fun getServerInfo(msid: String, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            MuServerInfoPacket(callback.value!!)
        }else{
            MuMsgWarnPacket("Server info could not be registered: ${callback.msg}", cid)
        }
    }

    fun getAvailableServerTypes(cid: Long = Random.nextCallId()): MuPacket{
        val target = ServerPool.getAvailableTypes()
        return MuMsgOKPacket(gson.toJson(target), cid)
    }

    fun createMuServer(ms: MCJEServer, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.regMuServer(ms)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Created", cid)
        }else{
            MuMsgErrPacket("MuServer Create Failed: ${callback.msg}", cid)
        }
    }

    fun importMuServer(ms: MCJEServer, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.importMuServer(ms)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Imported", cid)
        }else{
            MuMsgErrPacket("MuServer Import Failed: ${callback.msg}", cid)
        }
    }

    fun deleteMuServer(msid: String, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.delMuServer(msid)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Deleted", cid)
        }else{
            MuMsgErrPacket("MuServer Delete Failed: ${callback.msg}", cid)
        }

    }

    fun removeMuServer(msid: String, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.removeMuServer(msid)
        return if(callback.isOk){
            MuMsgOKPacket("MuServer Removed", cid)
        }else{
            MuMsgErrPacket("MuServer Remove Failed: ${callback.msg}", cid)
        }
    }

    // TODO: Unstable in ServerPool & MCJEServer
    fun startMuServer(msid: String, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            callback.value!!.startMuServer()
            MuMsgOKPacket("MuServer Started", cid)
        }else{
            MuMsgErrPacket("MuServer Start Failed: ${callback.msg}", cid)
        }
    }

    // TODO: Unstable in ServerPool & MCJEServer
    fun stopMuServer(msid: String, enforce: Boolean, cid: Long = Random.nextCallId()): MuPacket{
        val callback = ServerPool.getMuServer(msid)
        return if(callback.isOk){
            callback.value!!.stopMuServer(enforce)
            MuMsgOKPacket("MuServer Started", cid)
        }else{
            MuMsgErrPacket("MuServer Start Failed: ${callback.msg}", cid)
        }
    }
}