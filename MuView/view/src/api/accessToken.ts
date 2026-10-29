/**
 * ## 访问令牌(仅内存)
 *
 * 刻意**不落任何持久化**:不写 Cookie、不写 localStorage / sessionStorage。
 * 令牌只活在当前页面实例里 —— 刷新、关标签、重新进入 WebUI 都需要重新输入。
 *
 * 单独成模块是为了避免循环依赖:`session.ts` 与 `MuCoreConnector.ts` 都要用它。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
let accessToken: string = ""

/** 当前页面持有的令牌;没有则为空串。 */
export const getAccessToken = (): string => accessToken

/** 登录成功后写入;空串等于清除。 */
export const setAccessToken = (token: string | null | undefined): void => {
    accessToken = (token ?? "").trim()
}

/** 注销或鉴权失败时清除。 */
export const clearAccessToken = (): void => {
    accessToken = ""
}

/** 是否已持有令牌(不代表一定正确,正确性由服务端判定)。 */
export const hasAccessToken = (): boolean => accessToken.length > 0
