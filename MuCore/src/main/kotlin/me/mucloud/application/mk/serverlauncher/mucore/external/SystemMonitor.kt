package me.mucloud.application.mk.serverlauncher.mucore.external

import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import java.lang.management.ManagementFactory
import kotlin.time.Duration

object SystemMonitor{

    private var isActive: Boolean = false
    private val MonitorFlow: MutableStateFlow<StatusPacket> = MutableStateFlow(getCurrentStatus())

    private fun getCurrentStatus(): StatusPacket {
        val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val cpuUsage = os.cpuLoad * 100
        val memoryUsage = ((os.totalMemorySize - os.freeMemorySize).toDouble() / os.totalMemorySize) *100
        val serverPool = ServerPool
        val coreInfo = MuCoreMini.getMuCoreInfo()

        return StatusPacket(
            SystemStatus(cpuUsage, memoryUsage),
            ServerStatus(serverPool.getTotalMuServer(), serverPool.getOnlineMuServerCount(), serverPool.getOfflineMuServerCount()),
            coreInfo
        )
    }

    fun initMonitor(interval: Duration){
        if (isActive) return
        isActive = true
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            while(isActive){
                MonitorFlow.emit(getCurrentStatus())
                delay(interval)
            }
        }
    }

    fun getStatus(): StateFlow<StatusPacket> = MonitorFlow.asStateFlow()

    fun close(){
        isActive = false
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
