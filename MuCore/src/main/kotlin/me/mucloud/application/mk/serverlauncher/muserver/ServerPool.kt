package me.mucloud.application.mk.serverlauncher.muserver

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.mucloud.application.mk.serverlauncher.MuCoreMini
import me.mucloud.application.mk.serverlauncher.mucore.MuResult
import me.mucloud.application.mk.serverlauncher.mucore.MuStateResult
import me.mucloud.application.mk.serverlauncher.mucore.MuUtils.any
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.info
import me.mucloud.application.mk.serverlauncher.mucore.external.MuLogger.warn
import me.mucloud.application.mk.serverlauncher.muserver.StandardMCJEServerTypes.UNKNOWN
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.util.UUID

object ServerPool {

    private const val LOG_PREFIX = "MuServer.Pool"

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(MCJEServer::class.java, MCJEServerAdapter)
        .create()

    private val ServerTypePool = mutableListOf<MCJEServerType>()
    private val Pool = mutableListOf<MCJEServer>()

    init{
        if(!MuCoreMini.getMuCoreConfig().getServerFolder().exists()){
            MuCoreMini.getMuCoreConfig().getServerFolder().mkdir()
        }
    }

    fun importMuServer(ms: MCJEServer): MuStateResult{
        val callback = validate(ms.msi)
        if(!callback.isOk) return MuStateResult(false, "MCJEServer is invalid: ${callback.msg}")
        Pool.add(ms)
        return MuStateResult.OK
    }

    fun regMuServer(ms: MCJEServer): MuStateResult{
        val callback = importMuServer(ms)
        if(!callback.isOk) return MuStateResult(false, "MCJEServer is invalid: ${callback.msg}")
        ms.deploy()
        return MuStateResult.OK
    }

    fun validate(msi: MCJEServer.Info): MuStateResult {
        val hasSameName: Boolean = Pool.find { msi.name == it.msi.name } != null
        val hasSameLocation: Boolean = Pool.find { msi.msl == it.msi.msl } != null
        val hasSamePort: Boolean = Pool.find { msi.port == it.msi.port } != null

        return MuStateResult(
            any(hasSameName, hasSameLocation, hasSamePort),
            when{
                hasSameName -> ""
                hasSameLocation -> ""
                hasSamePort -> ""
                else -> null
            }
        )
    }

    fun delMuServer(msid: String): MuStateResult{
        val callback = getMuServer(msid)
        if(!callback.isOk){
            return MuStateResult(false, "MCJEServer could not removed: $msid")
        }
        val target = callback.value!!
        target.msi.msl.deleteRecursively()
        Pool.remove(target)
        return MuStateResult.OK
    }

    fun removeMuServer(msid: String): MuStateResult{
        val callback = getMuServer(msid)
        if(!callback.isOk){
            return MuStateResult(false, "MCJEServer could not removed: $msid")
        }
        val target = callback.value!!
        File(target.msi.msl, "MK-ServerLauncher.json").deleteRecursively()
        Pool.remove(target)
        return MuStateResult.OK
    }

    fun getMuServer(msid: String): MuResult<MCJEServer>{
        val target = Pool.find { msid == it.msi.msid }
        return if(target == null){
            MuResult(false, null, "MCJEServer not found: $msid")
        }else{
            MuResult(true, target)
        }
    }

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

    fun saveServers() = Pool.forEach(MCJEServer::save)

    fun getType(id: String): MCJEServerType = ServerTypePool.find { it.id == id } ?: UNKNOWN

    fun regType(type: MCJEServerType){
        if (ServerTypePool.contains(type)){
            warn(LOG_PREFIX, "Ambiguous Server Type Detected >> ${type.id}")
        }
    }

    fun randomMSID(): String {
        var rawId: String
        do{
            rawId = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        }while (!getMuServer(rawId).isOk)
        return rawId
    }
}


