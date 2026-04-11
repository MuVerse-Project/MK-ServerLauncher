package me.mucloud.application.mk.serverlauncher.muserver

enum class ServerStatus(val code: Int) {
    CREATED(0),
    STOPPED(1),
    PREPARING(2),
    RUNNING(3),
    STOPPING(4),
    RESTARTING(5),
    IN_ERROR(6);

    override fun toString(): String = code.toString()
}