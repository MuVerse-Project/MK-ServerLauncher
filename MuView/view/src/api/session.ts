/**
 * ## 会话状态(轻量单例)
 *
 * 路由守卫与登录页共用这一份状态,避免每次跳转都打一次接口。
 * 这里刻意不引入 Pinia:状态只有一个布尔值,用响应式对象足够,
 * 也免得在路由守卫里 await store 初始化。
 *
 * @since VoidLand V0 | DEV.1
 */
import {reactive} from "vue"
import {fetchSession, loginWithToken, logoutSession} from "@muapi/auth.ts"

export interface MuSessionState {
  /** 是否已登录 */
  authenticated: boolean
  /** 服务端是否启用鉴权(未启用时无需登录) */
  authEnabled: boolean
  /** 是否已完成首次探测 */
  resolved: boolean
}

export const muSession = reactive<MuSessionState>({
  authenticated: false,
  authEnabled: false,
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
 * @since VoidLand V0 | DEV.1
 */
export async function login(token: string): Promise<boolean> {
  try {
    const data = await loginWithToken(token)
    muSession.authenticated = data?.authenticated ?? true
    muSession.resolved = true
    return muSession.authenticated
  } catch {
    muSession.authenticated = false
    return false
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
    muSession.authenticated = false
    muSession.resolved = true
  }
}
