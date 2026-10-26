package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.mucloud.application.mk.serverlauncher.mupacket.mucore.internal.MuMsgErrPacket

/**
 * ## 访问守卫
 *
 * 只拦截**接口与 WebSocket**;静态资源与 SPA 路由放行,
 * 这样登录界面可以由前端自己渲染(与主界面同一套主题与组件)。
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
 * - `POST /api/v1/auth/login`   提交令牌,成功后写入会话 Cookie
 * - `POST /api/v1/auth/logout`  注销会话
 *
 * 这三个接口本身不经过 [MuViewAccessGuard] 的拦截判断,因为路径虽在 `/api/` 下,
 * 但必须允许未登录访问;放行逻辑由 [MuViewAuth.requiresAuth] 的调用方(守卫)负责,
 * 这里通过"会话接口白名单"实现。
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
                val body = runCatching { call.receive<Map<String, String>>() }.getOrNull()
                val provided = body?.get("token")
                if (!MuViewAuth.verify(provided)) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        MuMsgErrPacket("Invalid access token."),
                    )
                    return@post
                }
                MuViewAuth.issueSession(call)
                call.respond(mapOf("authenticated" to true))
            }

            post("/logout") {
                MuViewAuth.revokeSession(call)
                call.respond(mapOf("authenticated" to false))
            }
        }
    }
}
