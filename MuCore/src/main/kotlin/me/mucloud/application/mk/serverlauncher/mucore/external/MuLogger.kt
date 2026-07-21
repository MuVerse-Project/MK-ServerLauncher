package me.mucloud.application.mk.serverlauncher.mucore.external

import me.mucloud.application.mk.serverlauncher.MuCoreMini
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MuLogger {

    private val muLogger: Logger = LoggerFactory.getLogger(MuCoreMini.getMuCoreInfo().core)

    fun info(logPrefix: String, msg:String){
        muLogger.info("$logPrefix | $msg")
    }

    fun warn(logPrefix: String, msg:String){
        muLogger.warn("$logPrefix | $msg")
    }

    fun warn(logPrefix: String, msg:String, throwable: Throwable){
        muLogger.warn("$logPrefix | $msg", throwable)
    }

    fun err(logPrefix: String, msg:String){
        muLogger.error("$logPrefix | $msg")
    }

    fun err(logPrefix: String, msg:String, throwable: Throwable){
        muLogger.error("$logPrefix | $msg", throwable)
    }

}