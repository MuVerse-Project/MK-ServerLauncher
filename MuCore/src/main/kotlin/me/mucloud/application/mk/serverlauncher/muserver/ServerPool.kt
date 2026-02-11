package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.GsonBuilder
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muserver.StandardMCJEServerTypes.UNKNOWN
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets

object ServerPool {

    private val ServerTypePool = mutableListOf<MCJEServerType>()
    private val Pool = mutableListOf<MCJEServer>()

    fun addServer(server: MCJEServer){
        if (validate(server) == 0){
            Pool.add(server)
            server.save()
        }
    }

    fun validate(server: MCJEServer): Int {
        val hasSameName: Boolean = Pool.find { server.getName() == it.getName() } != null
        val hasSameLocation: Boolean = Pool.find { server.getFolder() == it.getFolder() } != null
        val hasSamePort: Boolean = Pool.find { server.getPort() == it.getPort() } != null

        return if(hasSameName){ 1 }
            else if(hasSameLocation){ 2 }
            else if(hasSamePort){ 3 }
            else{ 0 }
    }

    fun delServer(name: String): Boolean{
        val target = getServer(name) ?: return false
        target.getFolder().deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun removeServer(name: String): Boolean{
        val target = getServer(name) ?: return false
        File(target.getFolder(), "MK-ServerLauncher.json").deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun getServer(name: String): MCJEServer? = Pool.find { name == it.getName() }

    fun getServerList(): List<MCJEServer> = Pool

    fun getTotalServer(): Int = Pool.size

    fun getOnlineServerCount(): Int = Pool.filter { it.getStatus() == ServerStatus.RUNNING }.size

    fun getOfflineServerCount(): Int = Pool.filter { it.getStatus() == ServerStatus.STOPPED }.size

    fun getAvailableTypes() = ServerTypePool

    fun scanServer(){
        val gson = GsonBuilder().registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter).create()
        MuCoreMini.getMuCoreConfig().getServerFolder().listFiles().forEach fl@{ f ->
            if(f.isDirectory){
                info("Searching Directory >> $f")
                val target = f.listFiles().find { sf -> sf.name == "MK-ServerLauncher.json" }
                if(target == null){
                    warn("Skipped")
                }else{
                    info("Introspecting Server Description >> $f")
                    Pool.add(gson.fromJson(FileReader(target, StandardCharsets.UTF_8), object: com.google.gson.reflect.TypeToken<MCJEServer>(){}))
                }
            }
        }
    }

    fun saveServers(){ Pool.forEach { it.save() } }

    fun getType(id: String): MCJEServerType = ServerTypePool.find { it.id == id } ?: UNKNOWN

    fun addType(type: MCJEServerType){
        if (ServerTypePool.contains(type)){
            warn("Ambiguous Server Type Detected >> ${type.id}")
        }
    }
}


