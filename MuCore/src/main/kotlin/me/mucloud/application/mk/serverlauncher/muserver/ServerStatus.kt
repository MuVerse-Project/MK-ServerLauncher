package me.mucloud.application.mk.serverlauncher.muserver

/**
 * MuServer 的运行阶段。
 *
 * ## 状态 → 服务器实际在做什么
 *
 * | code | 状态 | 服务器实际阶段 | 谁推到这儿 |
 * |---|---|---|---|
 * | 0 | CREATING | 目录刚建好，core.jar 还没落地 | deploy() 入口 |
 * | 1 | STOPPED | 没有进程，可安全 start / delete | onProcessExit / 初始态 |
 * | 2 | PREPARING | 进程未起，正在跑 MuTasks（下载 / 装插件 / 改配置） | startMuServer() |
 * | 8 | STARTING | 进程已起，Done 还没出现，正在加载世界 | runProcess() 返回后 |
 * | 3 | RUNNING | 已匹配 Done，可以 say / stop | 收流协程 |
 * | 4 | STOPPING | 已发出 stop，进程还没退 | stopMuServer() |
 * | 5 | RESTARTING | 旧进程退出中，等它退完再起新的 | restartMuServer() |
 * | 6 | IN_ERROR | Beta：报错自动重启中 | 自动重试逻辑 |
 * | 7 | ERROR | 进程已死或命中失败关键字，需人工介入 | onProcessExit / 收流协程 |
 *
 * ## 两条铁律
 *
 * 1. **只有 onProcessExit 能把状态推到 STOPPED / ERROR**，同步段最多推到 STARTING / STOPPING
 * 2. **STARTING → RUNNING 只认 stdout 的 Done 信号**，不靠计时器猜
 *
 * @since VoidLand V0 | DEV.1
 * @author Mu_Mu
 */
enum class ServerStatus(val code: Int) {

    /**
     * 正创建 MuServer
     */
    CREATING(0),

    /**
     * MuServer 已停止
     */
    STOPPED(1),

    /**
     * MuServer 正在执行 MuTasks 中
     *
     * 前提条件：当 MuServer 中定义了一个或多个 MuTask
     */
    PREPARING(2),

    /**
     * 进程已经拉起来，但**还没有收到** `Done (x.xs)!`
     *
     * 这段时间里服务端正在加载世界、初始化插件，可能持续几十秒到几分钟。
     * 收到 stdout 上的 `Done (x.xs)!` 才转 [RUNNING]；命中失败关键字则转 [ERROR]。
     *
     * @since VoidLand V0 | DEV.1
     */
    STARTING(8),

    /**
     * MuServer 运行中
     *
     * 判据：stdout 上出现了 `Done (x.xs)! For help, type "help"`。
     * 只有到了这个状态才能 `say` / `stop`。
     */
    RUNNING(3), // 运行中

    /**
     * MuServer 正在停止中
     */
    STOPPING(4), // 正在停止中

    /**
     * MuServer 重新启动中
     */
    RESTARTING(5), // 重新启动中

    /**
     * ***Beta 测试版特性***
     *
     * 正在遇错重启中
     *
     * 前提条件：MuServer 设置了 “报错后自动重新启动服务器”
     */
    IN_ERROR(6),

    /**
     * 服务器遭遇错误
     *
     * 无前置条件
     *
     * ==============================
     *
     * ***Beta 测试版特性***
     *
     * 处于 [IN_ERROR] 状态中重启了一定次数后仍然错误
     *
     * 前置条件：MuServer 设置了 “报错后自动重新启动服务器”
     */
    ERROR(7),

    ;
    override fun toString(): String = code.toString()
}