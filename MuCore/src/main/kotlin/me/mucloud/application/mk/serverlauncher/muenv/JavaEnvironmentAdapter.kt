package me.mucloud.application.mk.serverlauncher.muenv

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 *  Java Environment Serializer
 */
object JavaEnvironmentAdapter: JsonSerializer<JavaEnvironment>, JsonDeserializer<JavaEnvironment> {

    override fun serialize(
        s: JavaEnvironment,
        t: Type,
        c: JsonSerializationContext): JsonElement =
        JsonObject().apply{
            addProperty("EV_NAME", s.name)
            addProperty("EV_VER", s.getVersionString())
            addProperty("EV_CODE", s.getVersion().code)
            addProperty("EV_LOC", s.getExecFolder().absolutePath)
        }

    override fun deserialize(
        j: JsonElement,
        t: Type,
        c: JsonDeserializationContext
    ): JavaEnvironment {
        check(j.isJsonObject) { "Expected a JsonObject, got $j"}
        val raw = j.asJsonObject
        check(raw.has("EV_NAME")) { "Expected a EV_NAME but empty"}
        check(raw.has("EV_LOC")) { "Expected a EV_LOC but empty"}
        val name = raw.get("EV_NAME").asString
        val loc = raw.get("EV_LOC").asString
        return JavaEnvironment(name, loc)
    }

}