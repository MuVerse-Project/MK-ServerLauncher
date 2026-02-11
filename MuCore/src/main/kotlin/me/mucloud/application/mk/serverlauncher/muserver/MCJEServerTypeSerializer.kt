package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object MCJEServerTypeSerializer: JsonSerializer<MCJEServerType> {
    override fun serialize(
        s: MCJEServerType,
        t: Type,
        c: JsonSerializationContext
    ): JsonObject = JsonObject().apply {
        addProperty("ID", s.id)
        addProperty("NAME", s.name)
        addProperty("DESC", s.desc)
    }
}