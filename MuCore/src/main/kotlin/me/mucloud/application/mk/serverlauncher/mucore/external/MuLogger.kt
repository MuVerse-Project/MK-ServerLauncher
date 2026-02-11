package me.mucloud.application.mk.serverlauncher.mucore.external

import me.mucloud.application.mk.serverlauncher.MuCoreMini
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MuLogger {

    private val muLogger: Logger = LoggerFactory.getLogger("MK-ServerLauncher | ${MuCoreMini.getMuCoreInfo().core} | ")

    fun info(msg:String){
        muLogger.info(msg)
    }

    fun warn(msg:String){
        muLogger.warn(msg)
    }

    fun err(msg:String, throwable: Throwable){
        muLogger.error(msg, throwable)
    }

}