package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import kotlinx.serialization.json.Json
import me.mucloud.application.mk.serverlauncher.mucore.MuUtils.all
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer

val createMuServerPacketInfo = object: MuPacketInfo<CreateMuServerPacket>{
    override val pid: String = "muview.muserver:create"

    override fun fromData(
        data: JsonObject,
        cid: Long
    ): CreateMuServerPacket {
        val valid = all(data.has("MSI"), data.has("MSSC"))
        if(valid){
            val msi = Json.decodeFromString<MCJEServer.Info>(data["MSI"].asString)
            val mssc = Json.decodeFromString<MCJEServer.StartupConfig>(data["MSSC"].asString)
            return CreateMuServerPacket(MCJEServer(msi, mssc), cid)
        }else{
            throw UnsupportedOperationException("Do not read MP_DATA from MuPacket")
        }
    }
}

val deleteMuServerPacketInfo = object : MuPacketInfo<DeleteMuServerPacket> {
    override val pid: String = "muview.muserver:delete"

    override fun fromData(data: JsonObject, cid: Long): DeleteMuServerPacket {
        if(data.has("msid")){
            val msid = data["msid"].asString
            return DeleteMuServerPacket(msid, cid)
        }else{
            throw UnsupportedOperationException("MSID: ${data["msid"].asString} not provided")
        }
    }
}

val importMuServerPacketInfo = object: MuPacketInfo<CreateMuServerPacket>{
    override val pid: String = "muview.muserver:import"

    override fun fromData(
        data: JsonObject,
        cid: Long
    ): CreateMuServerPacket {
        val valid = all(data.has("MSI"), data.has("MSSC"))
        if(valid){
            val msi = Json.decodeFromString<MCJEServer.Info>(data["MSI"].asString)
            val mssc = Json.decodeFromString<MCJEServer.StartupConfig>(data["MSSC"].asString)
            return CreateMuServerPacket(MCJEServer(msi, mssc), cid)
        }else{
            throw UnsupportedOperationException("Do not read MP_DATA from MuPacket")
        }
    }
}