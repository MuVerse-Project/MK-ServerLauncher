package me.mucloud.application.mk.serverlauncher.muserver

enum class ServerStatus(val code: Int) {
    STOPPED(0),
    PREPARING(1),
    RUNNING(2),
    STOPPING(3),
    RESTARTING(4),
    IN_ERROR(5);

    override fun toString(): String = code.toString()
}