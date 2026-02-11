package me.mucloud.application.mk.serverlauncher.muview.mulink

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.mucloud.application.mk.serverlauncher.mupacket.api.MuPacketFactory
import me.mucloud.application.mk.serverlauncher.muview.MuCore
import me.mucloud.application.mk.serverlauncher.muview.gson

fun Application.initSocket(){
    CoroutineScope(Dispatchers.IO).launch{
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", MuCore.getMuCoreConfig().muLinkPort)
        log.info("MuLink is Listening at ${serverSocket.localAddress}")
        while(true){
            val socket = serverSocket.accept()
            log.info("MuLink Connected!")
            launch{
                val receiveChannel = socket.openReadChannel()
                val writeChannel = socket.openWriteChannel(autoFlush = true)
                try{
                    while(true){
                        val rawMP = receiveChannel.readBuffer().readText()
                        MuPacketFactory.toPacket(gson.toJsonTree(rawMP).asJsonObject).operate()
                    }
                }catch(e:Exception){
                    log.error("MuLink Connect Failed!", e)
                    socket.close()
                }
            }
        }
    }
}