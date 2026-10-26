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
  user?: SessionUser
}

export interface LoginResponse {
  authenticated?: boolean
}

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
