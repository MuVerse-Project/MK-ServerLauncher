package me.mucloud.application.mk.serverlauncher.mucore.external

import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuStatusPacket
import me.mucloud.application.mk.serverlauncher.muserver.ServerPool
import java.lang.management.ManagementFactory
import kotlin.time.Duration

object SystemMonitor{

    private var isActive: Boolean = false
    private val MonitorFlow: MutableStateFlow<MuStatusPacket> = MutableStateFlow(getCurrentStatus())

    private fun getCurrentStatus(): MuStatusPacket {
        val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val cpuUsage = os.cpuLoad * 100
        val memoryUsage = ((os.totalMemorySize - os.freeMemorySize).toDouble() / os.totalMemorySize) *100
        val serverPool = ServerPool
        val coreInfo = MuCoreMini.getMuCoreInfo()

        return MuStatusPacket(
            StatusPacket(
                SystemStatus(cpuUsage, memoryUsage),
                ServerStatus(serverPool.getTotalMuServer(), serverPool.getOnlineMuServerCount(), serverPool.getOfflineMuServerCount()),
                coreInfo
            )
        )
    }

    fun initMonitor(interval: Duration){
        isActive = true
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            while(isActive){
                MonitorFlow.emit(getCurrentStatus())
                delay(interval)
            }
        }
    }

    fun getStatus(): StateFlow<MuStatusPacket> = MonitorFlow.asStateFlow()

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
