package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import io.ktor.http.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

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

    /** 用户自定义令牌的最小长度。 */
    const val MIN_TOKEN_LENGTH: Int = 16

    private val logger = LoggerFactory.getLogger(MuViewAuth::class.java)

    /** 无需登录即可访问的接口(登录 / 查会话 / 注销 / 取一次性临时令牌)。 */
    private val PUBLIC_API: Set<String> = setOf(
        "/api/v1/auth/session",
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
        "/api/v1/auth/bootstrap",
    )

    /** 配置中的访问令牌;为空表示"用户还没设置过",此时走一次性临时令牌。 */
    private val accessToken: String
        get() = MuCoreMini.getMuCoreConfig().getMuViewAccessToken()

    /**
     * 用户还没设令牌时,进程内生成的一次性临时令牌(**不落盘**、不写日志)。
     *
     * 只在内存里活着:重启就换一个,没设令牌的人重新看登录页即可。
     */
    @Volatile
    private var bootstrapToken: String? = null

    /** 当前真正生效的令牌:配置里有就用配置的,否则用内存里那枚临时的。 */
    private val effectiveToken: String
        get() = accessToken.ifBlank { bootstrapToken ?: "" }

    /** 是否仍处于"用户未设置令牌"的首次初始化状态。 */
    val isBootstrap: Boolean
        get() = accessToken.isBlank()

    /** 是否启用鉴权。默认开启:没配置令牌时也有临时令牌兜着。 */
    val enabled: Boolean
        get() = effectiveToken.isNotBlank()

    /** 当前令牌的指纹,用于写入会话。 */
    val currentFingerprint: String
        get() = fingerprint(effectiveToken)

    /** 会话签名密钥:由令牌派生,令牌一变,旧签名立即失效。 */
    val signingKey: ByteArray
        get() = sha256("MuViewSession|$effectiveToken")

    /**
     * 确保进程内有一枚可用的令牌。
     *
     * - 配置里已有令牌 → 什么都不做(临时令牌清空);
     * - 配置为空 → 生成一枚一次性临时令牌,只在内存里,由 WebUI 首次登录页展示。
     *
     * 日志里**不打印明文**,只说明去哪儿看。
     *
     * @since VoidLand V0 | DEV.1
     * @author Mu_Mu
     */
    fun ensureBootstrapToken() {
        if (accessToken.isNotBlank()) {
            bootstrapToken = null
            return
        }
        if (bootstrapToken != null) return
        bootstrapToken = generateToken()
        logger.info(
            "[MuView] 未配置访问令牌(MuViewAccessToken 为空),已生成一次性临时令牌:" +
                "请在本机打开 WebUI 登录页查看,登录后立即设置自己的令牌。"
        )
    }

    /** 取一次性临时令牌;仅在首次初始化状态下有值。 */
    fun bootstrapTokenOrNull(): String? = if (isBootstrap) bootstrapToken else null

    /**
     * 校验并写入用户自定义令牌。
     *
     * @return 失败原因;成功返回 null。成功后临时令牌作废、旧会话因签名密钥变化自动失效。
     *
     * @since VoidLand V0 | DEV.1
     * @author Mu_Mu
     */
    fun setAccessToken(raw: String): String? {
        val token = raw.trim()
        val invalid = validateToken(token)
        if (invalid != null) return invalid
        if (token == effectiveToken) return "新令牌不能与当前令牌相同"
        MuCoreMini.getMuCoreConfig().setMuViewAccessToken(token)
        bootstrapToken = null
        logger.info("[MuView] 访问令牌已更新,旧会话全部失效。")
        return null
    }

    /** 令牌强度校验(长度 + 字符集),返回失败原因或 null。 */
    private fun validateToken(token: String): String? {
        if (token.isEmpty()) return "令牌不能为空"
        if (token.length < MIN_TOKEN_LENGTH) return "令牌至少 ${MIN_TOKEN_LENGTH} 位"
        if (token.any { it.isWhitespace() }) return "令牌不能包含空白字符"
        if (token.all { it.isDigit() }) return "令牌不能是纯数字"
        return null
    }

    /** 生成 32 字节随机令牌(base64url,无填充,43 字符)。 */
    private fun generateToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

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
        val expected = effectiveToken
        if (expected.isBlank()) return false
        return MessageDigest.isEqual(
            provided.toByteArray(Charsets.UTF_8),
            expected.toByteArray(Charsets.UTF_8),
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
        // 1) 请求头优先:令牌只存在前端内存里,每次进入页面都要重新带上
        val header = call.request.headers[HttpHeaders.Authorization]
        if (header != null && header.startsWith("Bearer ", ignoreCase = true)) {
            if (verify(header.substring("Bearer ".length).trim())) return true
        }
        // 2) 查询参数:给不支持自定义请求头的客户端(浏览器 WebSocket、curl)留的口子
        val queryToken = call.request.queryParameters["token"]
        if (queryToken != null && verify(queryToken)) return true
        // 3) 兼容旧的会话 Cookie(现在不再签发,仅保留读取能力)
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
