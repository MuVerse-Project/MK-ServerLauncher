/**
 * ## 会话状态(轻量单例)
 *
 * 路由守卫与登录页共用这一份状态,避免每次跳转都打一次接口。
 * 这里刻意不引入 Pinia:状态只有几个布尔值,用响应式对象足够,
 * 也免得在路由守卫里 await store 初始化。
 *
 * @since VoidLand V0 | DEV.1
 */
import {reactive} from "vue"
import {fetchSession, loginWithToken, logoutSession} from "@muapi/auth.ts"
import {clearAccessToken, hasAccessToken, setAccessToken} from "@muapi/accessToken.ts"

export interface MuSessionState {
  /** 是否已登录 */
  authenticated: boolean
  /** 服务端是否启用鉴权(未启用时无需登录) */
  authEnabled: boolean
  /** 是否仍处于首次初始化:用户还没设置自己的令牌,必须先设置 */
  bootstrap: boolean
  /** 是否已完成首次探测 */
  resolved: boolean
}

export const muSession = reactive<MuSessionState>({
  authenticated: false,
  authEnabled: false,
  bootstrap: false,
  resolved: false,
})

/**
 * 拉取服务端会话状态(可强制刷新)。
 *
 * @since VoidLand V0 | DEV.1
 */
export async function refreshSession(force: boolean = false): Promise<MuSessionState> {
  if (muSession.resolved && !force) {
    return muSession
  }
  try {
    const data = await fetchSession()
    muSession.authEnabled = data?.authEnabled ?? false
    muSession.authenticated = data?.authenticated ?? false
    muSession.bootstrap = data?.bootstrap ?? false
    // 带着令牌去问,服务端说没登录 → 说明这枚令牌已失效(改过令牌),顺手清掉
    if (!muSession.authenticated && hasAccessToken()) {
      clearAccessToken()
    }
  } catch {
    // 接口不可用时不阻塞页面,按"未登录"处理
    muSession.authEnabled = true
    muSession.authenticated = false
  }
  muSession.resolved = true
  return muSession
}

/**
 * 用访问令牌登录。
 *
 * 失败时把「为什么失败」一起带回去：令牌不对是 401，撞上失败限流是 429，
 * 登录页要能分清这两种，不能一律显示"令牌不正确"。
 *
 * @since VoidLand V0 | DEV.1
 */
export interface MuLoginResult {
  ok: boolean
  /** 失败时的 HTTP 状态码 */
  status?: number
  /** 失败时服务端给的原文(MP_DATA.msg) */
  message?: string
  /** 429 时建议等待的秒数(来自 Retry-After) */
  retryAfter?: number
}

export async function login(token: string): Promise<MuLoginResult> {
  try {
    const data = await loginWithToken(token)
    // 令牌只放在内存里:刷新即失效,下次进入要重新输入
    setAccessToken(token)
    muSession.authenticated = data?.authenticated ?? true
    muSession.resolved = false
    // 登录后重新探测一次,拿到最新的 bootstrap 状态(决定要不要强制设置新令牌)
    await refreshSession(true)
    return {ok: muSession.authenticated}
  } catch (err) {
    clearAccessToken()
    muSession.authenticated = false
    const resp = (err as { response?: { status?: number; data?: unknown; headers?: Record<string, unknown> } })?.response
    const payload = resp?.data as { MP_DATA?: { msg?: string }; msg?: string } | string | undefined
    const message = typeof payload === "string" ? payload : payload?.MP_DATA?.msg ?? payload?.msg
    const seconds = Number(resp?.headers?.["retry-after"])
    return {
      ok: false,
      status: resp?.status,
      message,
      retryAfter: Number.isFinite(seconds) ? seconds : undefined,
    }
  }
}

/**
 * 注销会话。
 *
 * @since VoidLand V0 | DEV.1
 */
export async function logout(): Promise<void> {
  try {
    await logoutSession()
  } finally {
    clearAccessToken()
    muSession.authenticated = false
    muSession.bootstrap = false
    muSession.resolved = false
  }
}
