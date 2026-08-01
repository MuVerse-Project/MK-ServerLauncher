package me.mucloud.application.mk.serverlauncher.mucore

object MuUtils {

    fun any(vararg condition: Boolean): Boolean{
        condition.forEach { if (it) return true }
        return false
    }

    fun all(vararg condition: Boolean): Boolean{
        condition.forEach { if (!it) return false }
        return true
    }

}