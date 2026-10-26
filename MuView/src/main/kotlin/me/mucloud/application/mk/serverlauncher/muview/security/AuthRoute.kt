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
                    )
                )
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
                if (!MuViewAuth.verify(body?.get("token"))) {
                    LoginThrottle.recordFailure(client)
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        MuMsgErrPacket("Invalid access token."),
                    )
                    return@post
                }

                LoginThrottle.recordSuccess(client)
                call.sessions.set(
                    MuViewSession(
                        fingerprint = MuViewAuth.currentFingerprint,
                        issuedAt = System.currentTimeMillis() / 1000,
                    )
                )
                call.respond(mapOf("authenticated" to true))
            }

            post("/logout") {
                call.sessions.clear<MuViewSession>()
                call.respond(mapOf("authenticated" to false))
            }
        }
    }
}
