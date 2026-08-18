package me.mucloud.application.mk.serverlauncher.mupacket.muview

import com.google.gson.JsonObject
import kotlinx.serialization.json.Json
import me.mucloud.application.mk.serverlauncher.mucore.MuUtils.all
import me.mucloud.application.mk.serverlauncher.muenv.JavaEnvironment
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketInfo
import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import java.io.File

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

val importMuServerPacketInfo = object: MuPacketInfo<ImportMuServerPacket>{
    override val pid: String = "muview.muserver:import"

    override fun fromData(
        data: JsonObject,
        cid: Long
    ): ImportMuServerPacket {
        val valid = all(data.has("LOC"), data.has("MSSC"))
        if(valid){
            val loc = File(data["LOC"].asString)
            val mssc = Json.decodeFromString<MCJEServer.StartupConfig>(data["MSSC"].asString)
            return ImportMuServerPacket(loc, mssc, cid)
        }else{
            throw UnsupportedOperationException("Do not read MP_DATA from MuPacket")
        }
    }
}

val createMuEnvPacketInfo = object : MuPacketInfo<CreateMuEnvPacket> {
    override val pid: String = "muview.muenv:create"

    override fun fromData(
        data: JsonObject,
        cid: Long
    ): CreateMuEnvPacket {
        val vaild = all(data.has("name"), data.has("path"))
        if(vaild){
            val name = data["name"].asString
            val path = data["path"].asString
            return CreateMuEnvPacket(JavaEnvironment(name, path), cid)
        }else{
            throw UnsupportedOperationException("Do not read MP_DATA from MuPacket")
        }
    }
}