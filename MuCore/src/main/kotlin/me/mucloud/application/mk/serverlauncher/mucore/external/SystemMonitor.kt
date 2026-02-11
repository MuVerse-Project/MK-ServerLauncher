package me.mucloud.application.mk.serverlauncher.mucore.external

import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.concurrent.schedule

object SystemMonitor{

    private var Interval: Int = MuCoreMini.getMuCoreConfig().getSystemMonitorInterval() // Seconds
    private lateinit var Tsk: TimerTask
    private var MonitorFlow: MutableStateFlow<StatusPacket> = MutableStateFlow(getCurrentStatus())

    private fun getCurrentStatus(): StatusPacket {
        val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val cpuUsage = os.cpuLoad * 100
        val memoryUsage = ((os.totalMemorySize - os.freeMemorySize).toDouble() / os.totalMemorySize) *100
        val serverPool = ServerPool
        val coreInfo = MuCoreMini.getMuCoreInfo()

        return StatusPacket(
            SystemStatus(cpuUsage, memoryUsage),
            ServerStatus(serverPool.getTotalServer(), serverPool.getOnlineServerCount(), serverPool.getOfflineServerCount()),
            coreInfo
        )
    }

    fun initMonitor(){
        CoroutineScope(Dispatchers.IO).launch {
            Tsk = async {
                Timer().schedule(0L, Interval * 1000L) {
                    launch { MonitorFlow.emit(getCurrentStatus()) }
                }
            }.await()
        }
    }

    fun getStatus(): StateFlow<StatusPacket> = MonitorFlow.asStateFlow()

    fun close(){
        Tsk.cancel()
    }
}

@Serializable
data class StatusPacket(
    val systemStatus: SystemStatus,
    val serverStatus: ServerStatus,
    val appInfoStatus: AppInfoStatus
)

@Serializable
data class SystemStatus(
    val CpuUsage: Double,
    val MemUsage: Double,
)

@Serializable
data class ServerStatus(
    val totalServer: Int,
    val onlineServer: Int,
    val offlineServer: Int,
)

@Serializable
data class AppInfoStatus(
    val core: String,
    val ver: String,
)
