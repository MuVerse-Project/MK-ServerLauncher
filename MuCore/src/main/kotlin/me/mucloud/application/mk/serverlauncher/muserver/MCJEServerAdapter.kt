package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object MCJEServerAdapter: JsonSerializer<MCJEServer> {
    override fun serialize(s: MCJEServer, t: Type, c: JsonSerializationContext): JsonElement =
        JsonObject().apply{
            val targetMSI = s.msi
            val targetMSS = s.mss
            addProperty("name", targetMSI.name)
            addProperty("desc", targetMSI.name)
            addProperty("version", targetMSI.version)
            addProperty("type", targetMSI.type.id)
            addProperty("env", targetMSI.env.name)
            addProperty("status", targetMSS.code)
        }
}