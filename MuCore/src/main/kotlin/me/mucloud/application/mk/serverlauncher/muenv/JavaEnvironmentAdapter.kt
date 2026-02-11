package me.mucloud.application.mk.serverlauncher.muenv

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 *  Java Environment Serializer
 */
object JavaEnvironmentAdapter: JsonSerializer<JavaEnvironment>{

    override fun serialize(s: JavaEnvironment, t: Type, c: JsonSerializationContext): JsonElement  =
        JsonObject().apply{
            addProperty("EV_NAME", s.name)
            addProperty("EV_VER", s.getVersionString())
            addProperty("EV_CODE", s.getVersion().code)
            addProperty("EV_LOC", s.getExecFolder().absolutePath)
        }

}