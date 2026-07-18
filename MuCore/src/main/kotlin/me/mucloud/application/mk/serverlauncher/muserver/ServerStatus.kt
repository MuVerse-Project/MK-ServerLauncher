package me.mucloud.application.mk.serverlauncher.muserver

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
     * MuServer 运行中
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