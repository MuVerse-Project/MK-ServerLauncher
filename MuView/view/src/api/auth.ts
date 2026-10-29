import {MuHttpClient} from "@muapi/MuCoreConnector.ts"

export type UserRole = 'admin' | 'user' | string

export interface SessionUser {
  username: string
  role: UserRole
}

export interface SessionResponse {
  authenticated?: boolean
  /** 服务端是否启用了鉴权。未启用时前端不需要登录页。 */
  authEnabled?: boolean
  /** 是否仍处于首次初始化(用户还没设置自己的令牌)。 */
  bootstrap?: boolean
  user?: SessionUser
}

export interface LoginResponse {
  authenticated?: boolean
}

/**
 * 首次初始化状态下取一次性临时令牌(仅本机可获取)。
 *
 * @since VoidLand V0 | DEV.1
 */
export const fetchBootstrapToken = () =>
    MuHttpClient.getRaw<{ token?: string }>('/api/v1/auth/bootstrap')

/**
 * 设置新的访问令牌(需已登录)。成功后旧会话全部失效,需要重新登录。
 *
 * @since VoidLand V0 | DEV.1
 */
export const updateAccessToken = (token: string) =>
    MuHttpClient.postRaw<{ ok?: boolean; relogin?: boolean }>('/api/v1/auth/token', {token})

/**
 * 查询当前会话状态。
 *
 * @since VoidLand V0 | DEV.1
 */
export const fetchSession = () => MuHttpClient.getRaw<SessionResponse>('/api/v1/auth/session')

/**
 * 提交访问令牌换取会话。
 *
 * @since VoidLand V0 | DEV.1
 */
export const loginWithToken = (token: string) =>
    MuHttpClient.postRaw<LoginResponse>('/api/v1/auth/login', {token})

/**
 * 注销当前会话。
 *
 * @since VoidLand V0 | DEV.1
 */
export const logoutSession = () => MuHttpClient.postRaw<LoginResponse>('/api/v1/auth/logout', {})

export const canAccessRole = (userRole: UserRole | undefined, requiredRoles?: readonly UserRole[]) => {
  if (!requiredRoles?.length) {
    return true
  }

  return userRole != null && requiredRoles.includes(userRole)
}
