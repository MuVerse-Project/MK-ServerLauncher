package me.mucloud.application.mk.serverlauncher.mupacket.api

import com.google.gson.JsonObject
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

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
    private val pattern: Regex = Regex("^mu(core|view)(\\.[A-Za-z0-9\\-_]{2,})+:([A-Za-z0-9\\-_]{2,})(\\.[A-Za-z0-9\\-_]{2,})*$")
    private val MPPool: ConcurrentHashMap<String, MuPacketInfo<*>> = ConcurrentHashMap()
    private val MPListeners: ConcurrentHashMap<MuPacketInfo<*>, MutableList<MuPacket.() -> Unit>> = ConcurrentHashMap()

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
        check(!MPPool.containsKey(pid)) { "Invalid MuPacket: $pid >> Ambiguous MP_ID" }
        check(pattern.matches(pid)) { "Invalid MuPacket: $pid >> Invalid MP_ID" }
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
        // 先把请求号抠出来：解析失败时也要把它带回错误包，前端才能把错误对回某次调用
        var requestCid: Long? = null
        try{
            require(raw.has("MP_ID") && raw.has("MP_DATA") && raw.has("CID")) { "Invalid MuPacket Raw >> Corrupted Raw" }
            val cid = raw["CID"].asLong
            requestCid = cid
            val mpid = raw["MP_ID"].asString
            val type = MPPool[mpid] ?: error("Invalid MuPacket Raw >> Unregistered MP_ID ($mpid)")
            // 方向由接口表达：MuCore 只收 MuPacket2Core 的包。以前这一步是 fromData 抛异常。
            if (type !is MuPacket2Core) {
                warn("MuPacketFactory", "Rejected $mpid: it is a MuPacket2View packet")
                return MuMsgErrPacket(
                    "Invalid packet direction: $mpid should be sent to MuView",
                    cid,
                )
            }
            require(raw["MP_DATA"].isJsonObject) { "Invalid MuPacket Raw >> MP_DATA must be an json object" }
            val data = raw["MP_DATA"].asJsonObject
            return type.fromData(data, cid).also { callListeners(type, it) }
        }catch (e: Exception){
            warn("MuPacketFactory", "Deserialize failed: ${e.message}")
            return MuMsgErrPacket(
                "Invalid packet: ${e.message}",
                requestCid ?: Random.nextCallId(),
            )
        }
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
        check(MPPool.containsKey(type.pid)) { "Invalid MuPacket Listener >> Unregistered MuPacket ID: ${type.pid}" }
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
    private fun callListeners(type: MuPacketInfo<*>, mp: MuPacket) = MPListeners[type]?.forEach { it(mp) }

    private val CALL_SEQ: AtomicLong = AtomicLong(0)

    /**
     * ### MuPacket Call ID Generator
     *
     * 进程内单调自增：并发下不会撞号，将来接 pending 配对表可以直接用。
     * 不追求跨实例唯一——没有跨实例配对这种场景，加实例前缀只是往 CID 里塞语义。
     *
     * 保留 `Random.nextCallId()` 这个调用形态，免得把所有包类都改一遍。
     *
     * @author Mu_Cloud
     * @return A Long Number, which represents the Call ID of a MuPacket
     * @since RainyZone V1 | DEV.2
     */
    fun Random.nextCallId(): Long = CALL_SEQ.incrementAndGet()
}