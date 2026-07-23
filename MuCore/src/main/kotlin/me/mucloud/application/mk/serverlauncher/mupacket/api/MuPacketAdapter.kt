package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object MuPacketAdapter: JsonSerializer<MuPacket>, JsonDeserializer<MuPacket> {

    /**
     * # MuPacket Packer
     *
     * ***All of MuPacket must be as a [JsonObject]***
     *
     * @author Mu_Cloud
     * @since RainyZone V1 | DEV.1
     * @return A [JsonObject], but type is [JsonElement]
     */
    override fun serialize(
        s: MuPacket,
        t: Type,
        c: JsonSerializationContext
    ): JsonElement = s.toJson()

    /**
     * # MuPacket Reader
     *
     * ***If you want to read MuPacket as JsonObject to MuPacket, its MuPacketInfo must be registered by [MuPacketFactory.regMuPacket]***
     *
     * @author Mu_Cloud
     * @since RainyZone V1 | DEV.1
     * @return A [MuPacket] from provided JSON Object
     */
    override fun deserialize(
        j: JsonElement,
        t: Type,
        c: JsonDeserializationContext
    ): MuPacket {
        require(j.isJsonObject) { "MuPacket must be a JSON object" }
        val raw: JsonObject = j.asJsonObject
        return MuPacketFactory.toPacket(raw)
    }
}