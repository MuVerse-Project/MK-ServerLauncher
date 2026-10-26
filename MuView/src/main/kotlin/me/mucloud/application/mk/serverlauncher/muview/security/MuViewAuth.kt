package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * ## MuView 访问令牌
 *
 * 对整站做一层准入控制:未通过令牌验证的请求拿不到页面,也调不到接口。
 * 设计参照 NapCat / AstrBot WebUI 的做法 —— 配置里放一个令牌,打开页面时输入一次,
 * 之后靠会话 Cookie 保持登录。
 *
 * 为什么不用来源地址判断:MKSL 常部署在云服务器上,对外是域名 + 端口转发,
 * 甚至前置反向代理。反代转发来的请求来源就是 127.0.0.1,按地址判断会放行所有外部访问;
 * 而端口转发、跨网段场景又会被误拒。来源地址在这两种拓扑下都不代表身份,
 * 能跨部署形态生效的只有显式凭据。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
object MuViewAuth {

    /** 会话 Cookie 名。 */
    const val COOKIE_NAME: String = "MUVIEW_SESSION"

    /** 登录页路径。 */
    const val LOGIN_PATH: String = "/login"

    /** 登出路径。 */
    const val LOGOUT_PATH: String = "/logout"

    /** 会话有效期:12 小时。 */
    private const val SESSION_TTL_MS: Long = 12L * 60 * 60 * 1000

    private val sessions: MutableMap<String, Long> = ConcurrentHashMap()
    private val random: SecureRandom = SecureRandom()

    /** 配置中的访问令牌;为空表示不启用鉴权(仅建议在本机开发时使用)。 */
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

    private fun pruneExpired() {
        val now = System.currentTimeMillis()
        sessions.entries.removeIf { it.value < now }
    }

    /**
     * 判断请求是否应当以 JSON 形式返回 401(接口/WebSocket),而不是跳转登录页。
     *
     * @since VoidLand V0 | DEV.1
     */
    fun wantsJson(call: ApplicationCall): Boolean {
        val path = call.request.path()
        if (path.startsWith("/api/") || path == "/muclose") return true
        val upgrade = call.request.headers[HttpHeaders.Upgrade]
        return upgrade?.equals("websocket", ignoreCase = true) == true
    }

    /**
     * 未登录时的统一响应:接口 401,页面跳登录。
     *
     * @since VoidLand V0 | DEV.1
     */
    suspend fun reject(call: ApplicationCall) {
        if (wantsJson(call)) {
            call.respond(
                HttpStatusCode.Unauthorized,
                MuMsgErrPacket("Unauthorized: MuView access token required."),
            )
        } else {
            call.respondRedirect(LOGIN_PATH)
        }
    }

    /** 登录页 HTML(自包含,不依赖前端构建产物)。 */
    fun loginPage(error: String? = null): String = """
        |<!DOCTYPE html>
        |<html lang="zh-CN">
        |<head>
        |  <meta charset="utf-8">
        |  <meta name="viewport" content="width=device-width, initial-scale=1">
        |  <title>MuView · 需要访问令牌</title>
        |  <style>
        |    :root { color-scheme: dark; }
        |    body { margin:0; min-height:100vh; display:flex; align-items:center; justify-content:center;
        |           background:#0b0b0f; color:#e8e8ee;
        |           font-family:system-ui,-apple-system,"Segoe UI",Roboto,"Helvetica Neue",sans-serif; }
        |    .card { width:min(92vw,380px); padding:32px; border-radius:16px; background:#14141b;
        |            border:1px solid #23232e; box-shadow:0 24px 64px rgba(0,0,0,.5); }
        |    h1 { margin:0 0 8px; font-size:20px; font-weight:600; letter-spacing:.02em; }
        |    p  { margin:0 0 24px; font-size:13px; line-height:1.6; color:#8b8b9b; }
        |    input { width:100%; box-sizing:border-box; padding:12px 14px; border-radius:10px;
        |            border:1px solid #2c2c3a; background:#0f0f15; color:#e8e8ee; font-size:14px; }
        |    input:focus { outline:none; border-color:#7c5cff; }
        |    button { width:100%; margin-top:16px; padding:12px; border:0; border-radius:10px;
        |             background:#7c5cff; color:#fff; font-size:14px; font-weight:600; cursor:pointer; }
        |    button:hover { background:#6b4df0; }
        |    .err { margin-top:14px; font-size:13px; color:#ff6b6b; }
        |  </style>
        |</head>
        |<body>
        |  <form class="card" method="post" action="$LOGIN_PATH">
        |    <h1>MuView</h1>
        |    <p>请输入访问令牌。令牌配置在服务端 <code>MK-ServerLauncher.yml</code> 的 <code>MuViewAccessToken</code> 中。</p>
        |    <input type="password" name="token" placeholder="访问令牌" autocomplete="current-password" autofocus required>
        |    <button type="submit">进入</button>
        |    ${if (error != null) "<div class=\"err\">${error}</div>" else ""}
        |  </form>
        |</body>
        |</html>
    """.trimMargin()
}
