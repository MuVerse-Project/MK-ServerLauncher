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

    fun regMuServer(ms: MCJEServer){
        require(validate(ms.msi) == 0){ "MCJEServer is invalid!" }
        Pool.add(ms)
        ms.deploy()
    }

    fun importMuServer(ms: MCJEServer){

    }

    fun validate(msi: MCJEServer.Info): Int {
        val hasSameName: Boolean = Pool.find { msi.name == it.msi.name } != null
        val hasSameLocation: Boolean = Pool.find { msi.msl == it.msi.msl } != null
        val hasSamePort: Boolean = Pool.find { msi.port == it.msi.port } != null

        return if(hasSameName){ 1 }
            else if(hasSameLocation){ 2 }
            else if(hasSamePort){ 3 }
            else{ 0 }
    }

    fun delMuServer(name: String): Boolean{
        val target = getMuServer(name) ?: return false
        target.msi.msl.deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun removeMuServer(name: String): Boolean{
        val target = getMuServer(name) ?: return false
        File(target.msi.msl, "MK-ServerLauncher.json").deleteRecursively()
        Pool.remove(target)
        return true
    }

    fun getMuServer(name: String): MCJEServer? = Pool.find { name == it.msi.name }

    fun getMuServerList(): List<MCJEServer> = Pool

    fun getTotalMuServer(): Int = Pool.size

    fun getOnlineMuServerCount(): Int = Pool.filter { it.mss == ServerStatus.RUNNING }.size

    fun getOfflineMuServerCount(): Int = Pool.filter { it.mss == ServerStatus.STOPPED }.size

    fun getAvailableTypes() = ServerTypePool

    fun scanMuServer(){
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

    fun saveServers(){ Pool.forEach(TODO()) }

    fun getType(id: String): MCJEServerType = ServerTypePool.find { it.id == id } ?: UNKNOWN

    fun regType(type: MCJEServerType){
        if (ServerTypePool.contains(type)){
            warn(LOG_PREFIX, "Ambiguous Server Type Detected >> ${type.id}")
        }
    }
}


