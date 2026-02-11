package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object MCJEServerAdapter: JsonSerializer<MCJEServer> {
    override fun serialize(s: MCJEServer, t: Type, c: JsonSerializationContext): JsonElement =
        JsonObject().apply{
            addProperty("name", s.getName())
            addProperty("desc", s.getDescription())
            addProperty("version", s.getVersion())
            addProperty("type", s.getType().id)
            addProperty("port", s.getPort())
            addProperty("env", s.getEnv().name)
            add("before_works", c.serialize(s.getBeforeWorks()))
            addProperty("location", s.getFolder().absolutePath)
        }
}