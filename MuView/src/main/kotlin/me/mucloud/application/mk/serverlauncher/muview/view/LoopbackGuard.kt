package me.mucloud.application.mk.serverlauncher.muview.view

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin

/**
 * 本机来源的白名单(回环地址的各种写法)。
 *
 * @since 0-beta1
 * @author Mu_Mu
 */
private val LOOPBACK_HOSTS: Set<String> = setOf(
    "127.0.0.1",
    "localhost",
    "::1",
    "0:0:0:0:0:0:0:1",
    "[::1]",
)

/**
 * 判断当前请求是否来自本机(回环地址)。
 *
 * MuView 的关闭接口属于"进程级"操作,不应该被局域网或公网上的任意客户端触发,
 * 因此只放行 loopback 来源。这里刻意**不读取** `X-Forwarded-For` / `X-Real-IP`
 * 之类的转发头:那些头可以被客户端随意伪造,只有确认前面确有反代时才应启用。
 *
 * @return 来源是回环地址时返回 true
 * @since 0-beta1
 * @author Mu_Mu
 * @see io.ktor.server.request.origin
 */
internal fun ApplicationCall.isLoopbackRequest(): Boolean {
    val remoteHost = request.origin.remoteHost
    return normalizeHost(remoteHost) in LOOPBACK_HOSTS
}

/**
 * 归一化主机字符串:`::ffff:127.0.0.1` 这类 IPv4-mapped 地址会被还原成 `127.0.0.1`。
 *
 * @since 0-beta1
 * @author Mu_Mu
 */
private fun normalizeHost(host: String): String {
    val trimmed = host.trim().lowercase()
    return if (trimmed.startsWith("::ffff:")) trimmed.removePrefix("::ffff:") else trimmed
}
