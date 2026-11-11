package me.mucloud.application.mk.serverlauncher.mucore

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 应用根目录。
 *
 * 配置、日志、服务器目录都从这个根往下解析，**不看 `user.dir`**。
 * `File("MK-ServerLauncher.yml")` 解析的是进程工作目录：从别的目录 `java -jar`、被启动器/计划任务拉起、
 * 或者 IDE 里调试，指向的地方都不一样，两个实例还会写同一份文件互相覆盖。
 *
 * 解析顺序：
 * 1. 系统属性 `-Dmksl.home=<路径>`（测试与特殊部署用）
 * 2. 环境变量 `MKSL_HOME`
 * 3. 跟随代码来源：发行态取 fat jar 的父目录；开发态沿目录向上找到含 `settings.gradle.kts` 的仓库根
 * 4. 兜底当前工作目录（并告警）
 *
 * @since VoidLand V0 | DEV.2
 * @author Mu_Mu
 */
object MuAppDir {

    const val PROPERTY: String = "mksl.home"
    const val ENV: String = "MKSL_HOME"

    private const val MAX_ASCEND = 8

    /** 应用根目录，只解析一次。 */
    val root: Path by lazy { resolve() }

    private fun resolve(): Path {
        System.getProperty(PROPERTY)
            ?.takeIf { it.isNotBlank() }
            ?.let { return Paths.get(it).toAbsolutePath().normalize() }

        System.getenv(ENV)
            ?.takeIf { it.isNotBlank() }
            ?.let { return Paths.get(it).toAbsolutePath().normalize() }

        val location = MuAppDir::class.java.protectionDomain?.codeSource?.location
        val codePath = location?.let { runCatching { Paths.get(it.toURI()) }.getOrNull() }

        if (codePath != null) {
            // 发行态：codeSource 指向 xxx-all.jar，取它的父目录
            if (Files.isRegularFile(codePath)) {
                return codePath.parent ?: codePath
            }
            // 开发态：codeSource 指向 build/classes/kotlin/main，向上找仓库根
            findProjectRoot(codePath)?.let { return it }
        }

        System.err.println("[MuAppDir] 无法从代码位置推断应用目录，退回工作目录：${Paths.get("").toAbsolutePath()}")
        return Paths.get("").toAbsolutePath()
    }

    private fun findProjectRoot(start: Path): Path? {
        var cursor: Path? = start
        repeat(MAX_ASCEND) {
            val current = cursor ?: return null
            if (Files.exists(current.resolve("settings.gradle.kts")) ||
                Files.exists(current.resolve("settings.gradle"))
            ) {
                return current
            }
            cursor = current.parent
        }
        return null
    }

    /**
     * 目录能否写入：能创建的话，用临时文件探一次。
     *
     * 装在 Program Files 这类只读位置时，配置写不进去——这里只做判断，不擅自改路径。
     */
    fun isWritableDir(dir: Path): Boolean = try {
        Files.createDirectories(dir)
        val probe = Files.createTempFile(dir, ".writable", null)
        Files.deleteIfExists(probe)
        true
    } catch (_: Exception) {
        false
    }
}
