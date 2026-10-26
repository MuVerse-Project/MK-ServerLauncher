package me.mucloud.application.mk.serverlauncher.muview.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * ## 访问守卫
 *
 * 在路由之前拦一道:启用鉴权后,除了登录/登出路径,任何未登录请求都拿不到内容。
 * 接口与 WebSocket 返回 401,页面请求跳转到登录页。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
val MuViewAccessGuard = createApplicationPlugin("MuViewAccessGuard") {
    onCall { call ->
        if (!MuViewAuth.enabled) return@onCall
        val path = call.request.path()
        if (path == MuViewAuth.LOGIN_PATH || path == MuViewAuth.LOGOUT_PATH) return@onCall
        if (MuViewAuth.isAuthorized(call)) return@onCall
        MuViewAuth.reject(call)
    }
}

/**
 * 注册登录/登出路由。
 *
 * - `GET  /login`  展示登录页
 * - `POST /login`  校验令牌并签发会话
 * - `GET|POST /logout` 注销会话
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
fun Application.initAuthRoute() {
    routing {
        get(MuViewAuth.LOGIN_PATH) {
            call.respondText(MuViewAuth.loginPage(), ContentType.Text.Html)
        }

        post(MuViewAuth.LOGIN_PATH) {
            val form = call.receiveParameters()
            val provided = form["token"]
            if (MuViewAuth.verify(provided)) {
                MuViewAuth.issueSession(call)
                call.respondRedirect("/")
            } else {
                // 不给提示"令牌错误"以外的东西,避免额外信息泄露
                call.respondText(
                    MuViewAuth.loginPage("令牌不正确"),
                    ContentType.Text.Html,
                    HttpStatusCode.Unauthorized,
                )
            }
        }

        get(MuViewAuth.LOGOUT_PATH) {
            MuViewAuth.revokeSession(call)
            call.respondRedirect(MuViewAuth.LOGIN_PATH)
        }
    }
}
