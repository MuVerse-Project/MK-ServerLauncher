package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * ## MuView 访问令牌
 *
 * 对**接口层**做准入控制:未通过令牌验证拿不到任何数据。
 *
 * 登录界面由前端提供(`/login` 路由 + LoginView),后端只负责:
 * `GET /api/v1/auth/session` 查状态、`POST /api/v1/auth/login` 校验令牌、`POST /api/v1/auth/logout` 注销。
 * 这样登录页与主界面共用同一套组件、主题与 i18n,不会出现"两套界面"的割裂感;
 * 而静态资源本身不含任何数据,放行它们是安全的。
 *
 * 为什么不用来源地址判断:MKSL 常部署在云服务器上,对外是域名 + 端口转发,
 * 甚至前置反向代理。反代转发来的请求来源就是 127.0.0.1,按地址判断会放行所有外部访问;
 * 而端口转发、跨网段场景又会被误拒。来源地址在这两种拓扑下都不代表身份。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
object MuViewAuth {

    /** 会话 Cookie 名。 */
    const val COOKIE_NAME: String = "MUVIEW_SESSION"

    /** 无需登录即可访问的接口(登录/查会话/注销)。 */
    private val PUBLIC_API: Set<String> = setOf(
        "/api/v1/auth/session",
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
    )

    /** 会话有效期:12 小时。 */
    private const val SESSION_TTL_MS: Long = 12L * 60 * 60 * 1000

    private val sessions: MutableMap<String, Long> = ConcurrentHashMap()
    private val random: SecureRandom = SecureRandom()

    /** 配置中的访问令牌;为空表示不启用鉴权(仅建议本机开发时使用)。 */
    private val accessToken: String
        get() = MuCoreMini.getMuCoreConfig().getMuViewAccessToken()

    /** 是否启用鉴权。 */
    val enabled: Boolean
        get() = accessToken.isNotBlank()

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
     * 校验通过后签发会话,并写入 HttpOnly Cookie。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun issueSession(call: ApplicationCall) {
        val id = ByteArray(24).also(random::nextBytes)
            .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
        sessions[id] = System.currentTimeMillis() + SESSION_TTL_MS
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = id,
                path = "/",
                httpOnly = true,
                extensions = mapOf("SameSite" to "Lax"),
            )
        )
        pruneExpired()
    }

    /**
     * 当前请求是否已登录。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun isAuthorized(call: ApplicationCall): Boolean {
        val id = call.request.cookies[COOKIE_NAME] ?: return false
        val expireAt = sessions[id] ?: return false
        if (expireAt < System.currentTimeMillis()) {
            sessions.remove(id)
            return false
        }
        return true
    }

    /**
     * 注销会话并清除 Cookie。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun revokeSession(call: ApplicationCall) {
        call.request.cookies[COOKIE_NAME]?.let(sessions::remove)
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = "",
                path = "/",
                maxAge = 0,
                httpOnly = true,
            )
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

    private fun pruneExpired() {
        val now = System.currentTimeMillis()
        sessions.entries.removeIf { it.value < now }
    }
}
