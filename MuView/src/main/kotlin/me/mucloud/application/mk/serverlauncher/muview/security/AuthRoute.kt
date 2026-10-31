package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket

/**
 * ## 访问守卫
 *
 * 只拦截**接口与 WebSocket**;静态资源与 SPA 路由放行,
 * 登录界面由前端自己渲染(与主界面同一套主题与组件)。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
val MuViewAccessGuard = createApplicationPlugin("MuViewAccessGuard") {
    onCall { call ->
        if (!MuViewAuth.enabled) return@onCall
        if (!MuViewAuth.requiresAuth(call)) return@onCall
        if (MuViewAuth.isAuthorized(call)) return@onCall
        call.respond(
            HttpStatusCode.Unauthorized,
            MuMsgErrPacket("Unauthorized: MuView access token required."),
        )
    }
}

/**
 * 注册鉴权接口。
 *
 * - `GET  /api/v1/auth/session` 查询当前会话
 * - `POST /api/v1/auth/login`   提交令牌,成功后写入签名会话 Cookie
 * - `POST /api/v1/auth/logout`  注销会话
 *
 * 登录失败按来源限流:连续失败 5 次封锁 1 分钟,成功即清零。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
fun Application.initAuthRoute() {
    routing {
        route("/api/v1/auth") {
            get("/session") {
                call.respond(
                    mapOf(
                        "authenticated" to MuViewAuth.isAuthorized(call),
                        "authEnabled" to MuViewAuth.enabled,
                        // 还没设置自己的令牌 → 前端要引导"先设置令牌"
                        "bootstrap" to MuViewAuth.isBootstrap,
                    )
                )
            }

            // 首次初始化时才给的一次性临时令牌:仅本机来源可见,不落盘、不写日志。
            get("/bootstrap") {
                if (!MuViewAuth.isBootstrap) {
                    call.respond(HttpStatusCode.NotFound, MuMsgErrPacket("MuView access token already configured."))
                    return@get
                }
                if (!isLoopbackCall(call)) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        MuMsgErrPacket("Initial token is only visible from the local machine. Configure MuViewAccessToken in MK-ServerLauncher.yml instead."),
                    )
                    return@get
                }
                val token = MuViewAuth.bootstrapTokenOrNull()
                if (token == null) {
                    call.respond(HttpStatusCode.NotFound, MuMsgErrPacket("No bootstrap token available."))
                    return@get
                }
                call.respond(mapOf("token" to token))
            }

            post("/login") {
                val client = LoginThrottle.clientKey(call)
                LoginThrottle.blockedFor(client)?.let { seconds ->
                    call.response.headers.append(HttpHeaders.RetryAfter, seconds.toString())
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        MuMsgErrPacket("Too many failed attempts, retry in ${seconds}s."),
                    )
                    return@post
                }

                val body = runCatching { call.receive<Map<String, String>>() }.getOrNull()
                if (!MuViewAuth.verify(body?.get("token")?.trim())) {
                    LoginThrottle.recordFailure(client)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        MuMsgErrPacket("Invalid access token."),
                    )
                    return@post
                }

                LoginThrottle.recordSuccess(client)
                // 注意:不再写会话 Cookie。令牌只存在前端内存里,刷新/重进页面必须重新输入。
                call.respond(mapOf("authenticated" to true))
            }

            post("/logout") {
                call.sessions.clear<MuViewSession>()
                call.respond(mapOf("authenticated" to false))
            }

            // 已登录用户设置自己的令牌;成功后旧会话全部失效(签名密钥由令牌派生)。
            post("/token") {
                if (!MuViewAuth.isAuthorized(call)) {
                    call.respond(HttpStatusCode.Unauthorized, MuMsgErrPacket("Unauthorized: MuView access token required."))
                    return@post
                }
                val body = runCatching { call.receive<Map<String, String>>() }.getOrNull()
                // 已经设置过令牌的情况:必须再交一次当前令牌,避免"谁碰到已登录的页面就能改掉令牌"
                if (!MuViewAuth.isBootstrap && !MuViewAuth.verify(body?.get("currentToken")?.trim())) {
                    LoginThrottle.recordFailure(LoginThrottle.clientKey(call))
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MuMsgErrPacket("Current access token is incorrect."),
                    )
                    return@post
                }
                val error = MuViewAuth.setAccessToken(body?.get("token") ?: "")
                if (error != null) {
                    call.respond(HttpStatusCode.BadRequest, MuMsgErrPacket(error))
                    return@post
                }
                call.sessions.clear<MuViewSession>()
                call.respond(mapOf("ok" to true, "relogin" to true))
            }
        }
    }
}

/**
 * 是否来自本机(直连)。
 *
 * 只取 socket 地址、不看 `X-Forwarded-For`:反代场景下头部可伪造,
 * 而首次初始化本来就该在本机做 —— 远端部署请在配置文件里直接写令牌。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
private fun isLoopbackCall(call: ApplicationCall): Boolean {
    val raw = call.request.local.remoteHost
    if (isLoopbackHost(raw)) return true
    // 兜底:部分 JDK / 平台会把 127.0.0.1 报成 IPv6 映射形式,按地址再判一次
    val byAddress = runCatching {
        java.net.InetAddress.getByName(raw.trim().removePrefix("/")).isLoopbackAddress
    }.getOrDefault(false)
    if (!byAddress) {
        org.slf4j.LoggerFactory.getLogger("MuViewAuth")
            .warn("[MuView] 拒绝远程读取临时令牌,来源地址 = $raw")
    }
    return byAddress
}

/**
 * 判断来源地址是不是本机。
 *
 * 兼容这些写法:`127.0.0.1`、`127.0.0.2`、`::1`、`0:0:0:0:0:0:0:1`、
 * `::ffff:127.0.0.1`、`[::1]`、`localhost`,以及带前导 `/` 的形式。
 *
 * @since VoidLand V0 | DEV.1
 */
private fun isLoopbackHost(raw: String): Boolean {
    val host = raw.trim().removePrefix("/").removeSurrounding("[", "]").lowercase()
    if (host.isEmpty()) return false
    if (host == "localhost" || host == "::1" || host == "0:0:0:0:0:0:0:1") return true
    if (host.startsWith("127.") || host.startsWith("::ffff:127.")) return true
    return false
}
