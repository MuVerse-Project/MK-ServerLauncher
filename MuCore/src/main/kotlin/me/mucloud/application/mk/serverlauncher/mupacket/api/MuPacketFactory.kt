package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.MPListeners
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.pattern
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory.toPacket
import java.util.concurrent.ConcurrentHashMap

/**
 * # MuPacket API
 *
 * ## MuPacketFactory
 *
 * MuPacket Register & MuPacket Deserializer
 *
 * @since RainyZone V1 | DEV.1
 * @author Mu_Cloud
 */
object MuPacketFactory {
    private val pattern: Regex = Regex("^mu(core|view)(\\.[A-Za-z0-9\\-_]{2,})+:([A-Za-z0-9\\-_]{2,})$")
    private val MPPool: ConcurrentHashMap<String, MuPacketInfo<out MuPacket>> = ConcurrentHashMap()
    private val MPListeners: ConcurrentHashMap<MuPacketInfo<out MuPacket>, MutableList<MuPacket.() -> Unit>> = ConcurrentHashMap()

    /**
     * ### MuPacket Register
     *
     * @author Mu_Cloud
     * @param type Implement of MuPacket Info
     * @throws IllegalArgumentException if MP_ID from [type] is not matches [pattern] or it was registered
     * @since RainyZone V1 | DEV.1
     */
    fun <T : MuPacket> regMuPacket(type: MuPacketInfo<T>) {
        val pid = type.pid
        require(!MPPool.containsKey(pid)) { "Invalid MuPacket ID: $pid >> Ambiguous MP_ID" }
        require(pattern.matches(pid)) { "Invalid MuPacket ID: $pid >> Ambiguous MP_ID" }
        MPPool[pid] = type
    }

    /**
     * ### Json2Packet | MuPacket Deserializer
     *
     * @author Mu_Cloud
     * @return A [MuPacket] from the JSON Message supplied.
     * @suppress Attention! It also calls the MuPacket Listeners if MP_ID is registered in [MPListeners]
     * @throws IllegalStateException if MuPacket Raw is corrupted or MP_ID is not registered
     * @since RainyZone V1 | DEV.1
     */
    fun toPacket(raw: JsonObject): MuPacket {
        check(raw.has("MP_ID") && raw.has("MP_DATA")) { "Invalid MuPacket Raw >> Corrupted Raw" }
        val mpid = raw["MP_ID"].asString
        val type = MPPool[mpid] ?: error("Invalid MuPacket Raw >> Unregistered MP_ID ($mpid)")
        val data = raw["MP_DATA"].asJsonObject
        return type.fromData(data).also { callListeners(type, it) }
    }

    /**
     * ### MuPacket Listener Register
     *
     * @author Mu_Cloud
     * @param type Implement of MuPacket Info
     * @param listener A Lambda function that will be executed in a [toPacket] call with the same [type].
     * @throws IllegalArgumentException if MP_ID from [type] is not registered
     * @since RainyZone V1 | DEV.1
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: MuPacket> regMuPacketListener(type: MuPacketInfo<T>, listener: T.() -> Unit) {
        require(MPPool.containsValue(type)) { "Invalid MuPacket Listener >> Unregistered MuPacket ID: ${type.pid}" }
        val target = MPListeners[type] ?: mutableListOf()
        target.add(listener as MuPacket.() -> Unit)
        MPListeners[type] = target
    }

    /**
     * ### MuPacket Listener Caller
     *
     * @author Mu_Cloud
     * @param type Implement of MuPacket Info
     * @param mp A [MuPacket] for serving its contents.
     * @since RainyZone V1 | DEV.1
     */
    private fun callListeners(type: MuPacketInfo<out MuPacket>, mp: MuPacket) {
        if(MPListeners.containsKey(type)) {
            MPListeners[type]?.forEach { it(mp) }
        }
    }

    /**
     * ### MuPacket Gson Adapter Register
     *
     * regMuPacket to Gson Builder Adapter Provider with MuPacket Class
     *
     * @author Mu_Cloud
     * @param builder Raw Gson Builder
     * @return The Gson Builder [builder] after register
     * @since RainyZone V1 | DEV.1
     * @suppress Unstable API.
     */
    fun addMuPacketAdapter(builder: GsonBuilder): GsonBuilder = builder.apply {
        registerTypeAdapter(MuPacketInfo::class.java, MuPacketAdapter)
    }
}