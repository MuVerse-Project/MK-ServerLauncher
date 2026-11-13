package me.mucloud.application.mk.serverlauncher.mucore

import com.electronwill.nightconfig.core.file.FileConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 一份完整的设置快照。写入时整体替换，读侧永远拿到一致的组合。
 *
 * @since VoidLand V0 | DEV.2
 * @author Mu_Mu
 */
data class MuSettings(
    val configVersion: Int,
    val serverFolder: String,
    val logFolder: String,
    val monitorIntervalSeconds: Long,
    val muCorePort: Int,
    val muViewPort: Int,
    val muViewAccessToken: String
)

/**
 * 应用设置（`MK-ServerLauncher.yml`）。
 *
 * 载体仍是 yml：人可读、能手改、拷一个文件就能整包搬走。守三条纪律：
 *
 * 1. **位置只认 [MuAppDir]**，不看 `user.dir`。配置跟着应用走，从哪儿启动都落在同一处。
 * 2. **内存是权威**：启动装载一次；写入统一走 [update]（先落盘、再换内存），不监听文件变化。
 *    所以不存在"改了文件只生效一半"的情况——改配置 = 重启（或将来由设置页经 API 调用写入）。
 * 3. **每个键都有默认值与校验**：缺键、类型不对、端口越界都退默认值并告警，不让进程起不来。
 *
 * 写回保留注释与键顺序（整段文本改写 + 临时文件原子替换），不用 NightConfig 的 save()——
 * 它会把整份 YAML 重写、注释与键顺序全丢。
 *
 * @since VoidLand V0 | DEV.2
 * @author Mu_Mu
 */
class MuConfiguration {

    private val configFile: Path = MuAppDir.root.resolve(FILE_NAME)

    /**
     * 只用来读。写入不走它（见 [writeBack]），所以不开 autosave / autoreload：
     * 开着 autoreload 只会重载它自己的内存 map，不会回写这里的快照，反而让"到底以谁为准"变得含糊。
     */
    private val fileConfig: FileConfig = FileConfig.builder(configFile)
        .defaultResource("/$FILE_NAME")
        .build()
        .also { it.load() }

    /** 当前生效的设置。写入时整体替换。 */
    @Volatile
    private var snapshot: MuSettings = loadSettings()

    init {
        if (!MuAppDir.isWritableDir(MuAppDir.root)) {
            // 直接停在这里：配置写不进去，后面每一次写回都会失败。
            // 与其让它在运行到改令牌那一步才炸，不如启动时把话说清楚。
            throw IllegalStateException(
                "应用目录不可写：${MuAppDir.root}。请把程序放到有写权限的目录，" +
                    "或用 -D${MuAppDir.PROPERTY}=<路径> 指定配置位置"
            )
        }
    }

    // ------------------------------------------------------------------ 读

    /**
     * MuView 的访问令牌（整站鉴权）。
     *
     * **留空 = 用户还没设置过令牌**：由服务端启动时生成一次性临时令牌放在内存里（不落盘），
     * WebUI 登录页展示一次；用户设定自己的令牌后才写回本文件。
     */
    fun getMuViewAccessToken(): String = snapshot.muViewAccessToken

    fun getServerFolder(): File = resolveFolder(snapshot.serverFolder)

    fun getLogFolder(): File = resolveFolder(snapshot.logFolder)

    fun getSystemMonitorInterval(): Duration = snapshot.monitorIntervalSeconds.seconds

    fun getMuCorePort(): Int = snapshot.muCorePort

    fun getMuViewPort(): Int = snapshot.muViewPort

    /** 配置文件的实际路径，排障与设置页展示用。 */
    fun getConfigFilePath(): Path = configFile

    // ------------------------------------------------------------------ 写

    /**
     * 写入新的访问令牌：先落盘，成功后换内存。失败时抛异常，调用方负责提示，内存保持不变。
     */
    @Synchronized
    fun setMuViewAccessToken(token: String) {
        update { it.copy(muViewAccessToken = token) }
    }

    /**
     * 设置页将来统一走这里：改内存 + 落盘，一步到底。
     *
     * @since VoidLand V0 | DEV.2
     */
    @Synchronized
    fun update(transform: (MuSettings) -> MuSettings) {
        val next = transform(snapshot)
        if (next == snapshot) return
        writeBack(next)
        snapshot = next
    }

    // ------------------------------------------------------------------ 内部

    private fun loadSettings(): MuSettings = MuSettings(
        configVersion = readLong(KEY_CONFIG_VERSION, DEFAULT_CONFIG_VERSION.toLong()).toInt(),
        serverFolder = readString(KEY_SERVER_FOLDER, DEFAULT_SERVER_FOLDER).ifBlank { DEFAULT_SERVER_FOLDER },
        logFolder = readString(KEY_LOG_FOLDER, DEFAULT_LOG_FOLDER).ifBlank { DEFAULT_LOG_FOLDER },
        monitorIntervalSeconds = readLong(KEY_MONITOR_INTERVAL, DEFAULT_MONITOR_INTERVAL).coerceAtLeast(1),
        muCorePort = readPort(KEY_MUCORE_PORT, DEFAULT_MUCORE_PORT),
        muViewPort = readPort(KEY_MUVIEW_PORT, DEFAULT_MUVIEW_PORT),
        muViewAccessToken = readString(KEY_MUVIEW_TOKEN, "")
    )

    private fun readRaw(key: String): Any? = runCatching { fileConfig.get<Any>(key) }.getOrNull()

    private fun readString(key: String, default: String): String =
        (readRaw(key) as? String) ?: default

    private fun readLong(key: String, default: Long): Long =
        (readRaw(key) as? Number)?.toLong()
            ?: (readRaw(key) as? String)?.trim()?.toLongOrNull()
            ?: default

    private fun readPort(key: String, default: Int): Int {
        val raw = readRaw(key)
        val value = (raw as? Number)?.toInt() ?: (raw as? String)?.trim()?.toIntOrNull()
        if (value == null) {
            warn("$key 缺失或不是数字，使用默认值 $default")
            return default
        }
        if (value !in 1..65535) {
            warn("$key = $value 超出 1..65535，使用默认值 $default")
            return default
        }
        return value
    }

    /** 相对路径按应用根解析，绝对路径原样返回。 */
    private fun resolveFolder(raw: String): File {
        val file = File(raw)
        return if (file.isAbsolute) file else MuAppDir.root.resolve(raw).toFile()
    }

    /**
     * 整份写回：逐键替换原文本里的那一行，其余行（注释、空行、别的键）原样保留，
     * 最后写临时文件再原子替换，避免写一半断电留下半截配置。
     */
    private fun writeBack(next: MuSettings) {
        val lines = if (Files.exists(configFile)) {
            Files.readAllLines(configFile, Charsets.UTF_8).toMutableList()
        } else {
            mutableListOf()
        }

        fun put(key: String, value: String) {
            val idx = lines.indexOfFirst { it.trimStart().startsWith("$key:") }
            if (idx >= 0) lines[idx] = "$key: $value" else lines.add("$key: $value")
        }

        put(KEY_CONFIG_VERSION, next.configVersion.toString())
        put(KEY_SERVER_FOLDER, quote(next.serverFolder))
        put(KEY_LOG_FOLDER, quote(next.logFolder))
        put(KEY_MONITOR_INTERVAL, next.monitorIntervalSeconds.toString())
        put(KEY_MUCORE_PORT, next.muCorePort.toString())
        put(KEY_MUVIEW_PORT, next.muViewPort.toString())
        put(KEY_MUVIEW_TOKEN, quote(next.muViewAccessToken))

        configFile.parent?.let { Files.createDirectories(it) }
        val tmp = configFile.resolveSibling("$FILE_NAME.tmp")
        Files.write(tmp, (lines.joinToString("\n") + "\n").toByteArray(Charsets.UTF_8))

        try {
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: Exception) {
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING)
        }
        // 让 reader 与磁盘保持一致（内存仍是权威，这一步只是防止两处说法不同）
        runCatching { fileConfig.load() }
    }

    private fun quote(value: String): String = "'" + value.replace("'", "''") + "'"

    private fun warn(message: String) {
        // 这里不能走 MuLogger：它在初始化时会回头取 MuCoreMini.getMuCoreInfo()，
        // 而 MuCoreMini 正持有本实例，容易形成初始化环。
        System.err.println("$LOG_PREFIX | $message")
    }

    companion object {
        private const val LOG_PREFIX = "[MuConfiguration]"
        private const val FILE_NAME = "MK-ServerLauncher.yml"

        private const val KEY_CONFIG_VERSION = "ConfigVersion"
        private const val KEY_SERVER_FOLDER = "ServerFolder"
        private const val KEY_LOG_FOLDER = "LogFolder"
        private const val KEY_MONITOR_INTERVAL = "SystemMonitorInterval"
        private const val KEY_MUCORE_PORT = "MuCorePort"
        private const val KEY_MUVIEW_PORT = "MuViewPort"
        private const val KEY_MUVIEW_TOKEN = "MuViewAccessToken"

        private const val DEFAULT_CONFIG_VERSION = 0
        private const val DEFAULT_SERVER_FOLDER = "servers"
        private const val DEFAULT_LOG_FOLDER = "logs"
        private const val DEFAULT_MONITOR_INTERVAL = 3L
        private const val DEFAULT_MUCORE_PORT = 20040
        private const val DEFAULT_MUVIEW_PORT = 20038
    }
}
