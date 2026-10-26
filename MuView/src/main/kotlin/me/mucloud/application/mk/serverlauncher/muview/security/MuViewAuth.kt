package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import io.ktor.http.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import kotlinx.serialization.Serializable
import java.security.MessageDigest

/**
 * ## 会话内容
 *
 * 只存一个"令牌指纹",不存令牌本身。会话由 Ktor 的 Session 插件签名后放进 Cookie,
 * 因此服务端无需保存状态:重启后只要令牌没变,登录状态依然有效。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
@Serializable
data class MuViewSession(
    /** 签发时使用的令牌指纹;与当前令牌不一致即视为失效(改令牌 = 全员重新登录)。 */
    val fingerprint: String,
    /** 签发时间(epoch 秒),便于排查与将来做更细的策略。 */
    val issuedAt: Long,
)

/**
 * ## MuView 访问令牌
 *
 * 对**接口层**做准入控制:未登录拿不到任何数据。登录界面由前端渲染(`/login` 路由),
 * 后端只提供 `session` / `login` / `logout` 三个接口。
 *
 * 为什么不用来源地址判断:MKSL 常部署在云服务器上,对外是域名 + 端口转发,甚至前置反向代理。
 * 反代转发来的请求来源就是 127.0.0.1,按地址判断会放行所有外部访问;端口转发、跨网段场景又会被误拒。
 *
 * 会话为什么能扛住重启又能被令牌变更立刻失效:
 * - 会话数据放在签名 Cookie 里(Ktor Sessions + HMAC),服务端不存表 → 重启不掉登录;
 * - 签名密钥由当前令牌派生 → 改令牌后旧 Cookie 既验不过签名,指纹也对不上。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
object MuViewAuth {

    /** 会话 Cookie 名。 */
    const val SESSION_NAME: String = "MUVIEW_SESSION"

    /** 会话有效期:12 小时。 */
    const val SESSION_MAX_AGE_SECONDS: Int = 12 * 60 * 60

    /** 无需登录即可访问的接口(登录 / 查会话 / 注销)。 */
    private val PUBLIC_API: Set<String> = setOf(
        "/api/v1/auth/session",
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
    )

    /** 配置中的访问令牌;为空表示不启用鉴权(仅建议本机开发时使用)。 */
    private val accessToken: String
        get() = MuCoreMini.getMuCoreConfig().getMuViewAccessToken()

    /** 是否启用鉴权。 */
    val enabled: Boolean
        get() = accessToken.isNotBlank()

    /** 当前令牌的指纹,用于写入会话。 */
    val currentFingerprint: String
        get() = fingerprint(accessToken)

    /** 会话签名密钥:由令牌派生,令牌一变,旧签名立即失效。 */
    val signingKey: ByteArray
        get() = sha256("MuViewSession|$accessToken")

    /**
     * 计算令牌指纹(取 SHA-256 前 16 个 hex 字符,足够区分、也不泄露令牌)。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun fingerprint(token: String): String =
        sha256(token).joinToString("") { "%02x".format(it) }.take(16)

    private fun sha256(text: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))

    /**
     * 校验调用方提交的令牌。
     *
     * 使用 [MessageDigest.isEqual] 比较,避免逐字节短路带来的时序差异。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun verify(provided: String?): Boolean {
        if (provided == null) return false
        return MessageDigest.isEqual(
            provided.toByteArray(Charsets.UTF_8),
            accessToken.toByteArray(Charsets.UTF_8),
        )
    }

    /**
     * 当前请求是否指向接口或 WebSocket —— 也就是真正需要守着的东西。
     *
     * 静态资源与 SPA 路由一律放行:它们只包含页面骨架,不含数据。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun requiresAuth(call: ApplicationCall): Boolean {
        val path = call.request.path()
        if (path in PUBLIC_API) return false
        if (path.startsWith("/api/") || path == "/muclose") return true
        val upgrade = call.request.headers[HttpHeaders.Upgrade]
        return upgrade?.equals("websocket", ignoreCase = true) == true
    }

    /**
     * 当前请求是否已登录(会话存在且指纹与当前令牌一致)。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun isAuthorized(call: ApplicationCall): Boolean {
        val session = call.sessions.get<MuViewSession>() ?: return false
        return session.fingerprint == currentFingerprint
    }
}

/**
 * ## 登录失败限流
 *
 * 按来源地址计数:连续失败若干次后短暂封锁,成功即清零。
 * 目标是挡掉"在线猜令牌",不需要做到登录失败审计那么重。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
object LoginThrottle {

    private const val MAX_FAILURES: Int = 5
    private const val WINDOW_MS: Long = 10 * 60 * 1000
    private const val BLOCK_MS: Long = 60 * 1000

    private data class State(var failures: Int, var firstFailureAt: Long, var blockedUntil: Long)

    private val states: MutableMap<String, State> = java.util.concurrent.ConcurrentHashMap()

    /** 若当前来源处于封锁中,返回剩余秒数;否则返回 null。 */
    fun blockedFor(clientKey: String): Long? {
        val state = states[clientKey] ?: return null
        val now = System.currentTimeMillis()
        if (state.blockedUntil > now) return (state.blockedUntil - now) / 1000 + 1
        return null
    }

    /** 记录一次失败;达到阈值则封锁。 */
    fun recordFailure(clientKey: String) {
        val now = System.currentTimeMillis()
        val state = states[clientKey]
        if (state == null || now - state.firstFailureAt > WINDOW_MS) {
            states[clientKey] = State(1, now, 0)
            return
        }
        state.failures += 1
        if (state.failures >= MAX_FAILURES) {
            state.blockedUntil = now + BLOCK_MS
            state.failures = 0
            state.firstFailureAt = now
        }
    }

    /** 登录成功后清零。 */
    fun recordSuccess(clientKey: String) {
        states.remove(clientKey)
    }

    /** 来源标识:优先取反代头,否则用连接地址。 */
    fun clientKey(call: ApplicationCall): String {
        val forwarded = call.request.headers["X-Forwarded-For"]
            ?.split(',')
            ?.firstOrNull()
            ?.trim()
        if (!forwarded.isNullOrBlank()) return forwarded
        return call.request.local.remoteHost
    }
}
