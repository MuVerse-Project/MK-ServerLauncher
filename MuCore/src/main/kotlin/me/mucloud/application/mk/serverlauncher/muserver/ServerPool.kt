package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muserver.StandardMCJEServerTypes.UNKNOWN
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets

object ServerPool {

    private const val LOG_PREFIX = "MuServer.Pool"

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
        .create()

    private val ServerTypePool = mutableListOf<MCJEServerType>()
    private val Pool = mutableListOf<MCJEServer>()



    fun addServer(server: MCJEServer){
        if (validate(server) == 0){
            Pool.add(server)
//            server.save()
        }
    }

    fun validate(server: MCJEServer): Int {
        val hasSameName: Boolean = Pool.find { server.msi.name == it.msi.name } != null
        val hasSameLocation: Boolean = Pool.find { server.msl == it.msl } != null
        val hasSamePort: Boolean = Pool.find { server.msi.port == it.msi.port } != null

        return if(hasSameName){ 1 }
            else if(hasSameLocation){ 2 }
            else if(hasSamePort){ 3 }
            else{ 0 }
    }

    fun delServer(name: String): Boolean{
        val target = getServer(name) ?: return false
        target.msl.deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun removeServer(name: String): Boolean{
        val target = getServer(name) ?: return false
        File(target.msl, "MK-ServerLauncher.json").deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun getServer(name: String): MCJEServer? = Pool.find { name == it.msi.name }

    fun getServerList(): List<MCJEServer> = Pool

    fun getTotalServer(): Int = Pool.size

    fun getOnlineServerCount(): Int = Pool.filter { it.mss == ServerStatus.RUNNING }.size

    fun getOfflineServerCount(): Int = Pool.filter { it.mss == ServerStatus.STOPPED }.size

    fun getAvailableTypes() = ServerTypePool

    fun scanServer(){
        MuCoreMini.getMuCoreConfig().getServerFolder().listFiles().forEach fl@{ f ->
            if(f.isDirectory){
                info(LOG_PREFIX, "Searching Directory >> $f")
                val target = f.listFiles().find { sf -> sf.name == "MK-ServerLauncher.json" }
                if(target == null){
                    warn(LOG_PREFIX, "Skipped")
                }else{
                    info(LOG_PREFIX, "Introspecting Server Description >> $f")
                    Pool.add(gson.fromJson(FileReader(target, StandardCharsets.UTF_8), object: TypeToken<MCJEServer>(){}))
                }
            }
        }
    }

    fun saveServers(){ Pool.forEach { /*it.save()*/ } }

    fun getType(id: String): MCJEServerType = ServerTypePool.find { it.id == id } ?: UNKNOWN

    fun addType(type: MCJEServerType){
        if (ServerTypePool.contains(type)){
            warn(LOG_PREFIX, "Ambiguous Server Type Detected >> ${type.id}")
        }
    }
}


