/**
 * ## 访问令牌(会话级)
 *
 * 存在 **sessionStorage**:刷新页面、在同一标签内跳转都还在;
 * 关掉标签/浏览器就消失,下次进来仍需重新输入。
 *
 * 不使用 localStorage —— 那个会跨浏览器重启长期保留,和"每次进入都要令牌"的意图不符。
 * 也不写 Cookie —— 服务端不再签发会话,令牌只由前端携带。
 *
 * 单独成模块是为了避免循环依赖:`session.ts` 与 `MuCoreConnector.ts` 都要用它。
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
const STORAGE_KEY = "MUVIEW_ACCESS_TOKEN"

const readStored = (): string => {
    try {
        return (window.sessionStorage.getItem(STORAGE_KEY) ?? "").trim()
    } catch {
        // 隐私模式 / 禁用存储时退化为纯内存
        return ""
    }
}

let accessToken: string = readStored()

/** 当前页面持有的令牌;没有则为空串。 */
export const getAccessToken = (): string => accessToken

/** 登录成功后写入;空串等于清除。 */
export const setAccessToken = (token: string | null | undefined): void => {
    accessToken = (token ?? "").trim()
    try {
        if (accessToken) {
            window.sessionStorage.setItem(STORAGE_KEY, accessToken)
        } else {
            window.sessionStorage.removeItem(STORAGE_KEY)
        }
    } catch {
        // 存不进去也不影响本次会话
    }
}

/** 注销、换令牌或鉴权失败时清除。 */
export const clearAccessToken = (): void => {
    accessToken = ""
    try {
        window.sessionStorage.removeItem(STORAGE_KEY)
    } catch {
        // 忽略
    }
}

/** 是否已持有令牌(不代表一定正确,正确性由服务端判定)。 */
export const hasAccessToken = (): boolean => accessToken.length > 0
