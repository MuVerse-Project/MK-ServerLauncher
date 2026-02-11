package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.jvmName

/**
 * # MuPacket API
 *
 * The Core Concept of MuPacketAPI
 *
 * @since TinyNova V1 | DEV.2
 * @author Mu_Cloud
 */
object MuPacketFactory {
    private val pattern: Regex = Regex("^mucore:([A-Za-z0-9\\-_]{3,}):([A-Za-z0-9\\-_]{3,})$")
    private val MPPool: ConcurrentHashMap<String, KClass<out AbstractMuPacket>> = ConcurrentHashMap()

    fun regMuPacket(mpClass: KClass<out AbstractMuPacket>) {
        // Check mpClass is Not Abstract Class
        if (mpClass.isAbstract) {
            throw UnsupportedOperationException("UnSupport Class Type ${mpClass.jvmName}: Abstract MuPacket is not supported")
        }
        val mpid = mpClass.createInstance().getPID()
        if(!pattern.matches(mpid)){
            throw UnsupportedOperationException("Invalid PID was used when creating a MuPacket.")
        }
        if(MPPool.containsKey(mpid)) {
            throw UnsupportedOperationException("Exist MuPacket ID: $mpid")
        }
        MPPool[mpid] = mpClass
    }

    fun toPacket(raw: JsonObject): MuPacket {
        try {
            if (!raw.has("MP_ID") || !raw.has("MP_DATA")) {
                throw UnsupportedOperationException("Detected Format Error in Received MuPacket RawJsonMessage Recently.")
            }
            val mpid = raw.get("MP_ID").asString
            if (!MPPool.containsKey(mpid)) {
                throw UnsupportedOperationException("The MP_ID: $mpid does not Registered in MuPacketAPI.")
            }
            val targetMPClass = MPPool[mpid]!!
            val targetMP = targetMPClass.createInstance()
            val mpData = raw.getAsJsonObject("MP_DATA")
            return targetMP.toPacket(mpData) ?: throw UnsupportedOperationException("Illegal MP_DATA for MuPacket ($mpid)")
        }catch (e: Exception){
            throw RuntimeException("Error Occurred when reading MuPacket", e)
        }
    }

}