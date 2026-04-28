package me.mucloud.application.mk.serverlauncher.muenv

import me.mucloud.application.mk.serverlauncher.muserver.MCJEServer
import java.util.regex.Pattern

/**
 * # | Minecraft & Java Compatibility Map
 *
 * - Used for marking Compatible Java Version for Specified Minecraft Version
 * - [Compatibility Information Source](https://minecraft.wiki/w/Tutorial:Setting_up_a_Java_Edition_server#Version_requirements)
 * - **Unstable, might be changed at any time. Not recommended for use or as a reference.**
 *
 * @since TinyNova V1 | DEV.1
 * @author Mu_Cloud
 */
enum class MCEnvCompMode(
    val pattern: Pattern,
    val leastCode: Int,
    val recommendCode: Int,
) {
    UNKNOWN(Pattern.compile(""), JavaVersion.final().code, JavaVersion.final().code), // Unknown, DO NOT USING BETA, PRE OR SNAPSHOT VERSION
    V1_8(Pattern.compile("^1\\.([7-9]|1[0-6])(\\.[1-9]\\d?)?"), 52, 52), // Java 8 at least, Java 8 Recommended.
    V1_17(Pattern.compile("^1\\.17(\\.[1-9]\\d?)?"), 60, 61), // Java 16 at least, Java 17 Recommended.
    V1_18(Pattern.compile("^1\\.1[8-9](\\.[1-9]\\d?)?"), 61, 61), // Java 17 at least, Java 17 Recommended.
    V1_20_R1(Pattern.compile("^1\\.20(\\.[1-4])?"), 61, 61), // Java 17 at least, Java 17 Recommended.
    V1_20_R2(Pattern.compile("^1\\.20\\.([5-9]|[1-9]\\d+)"), 65, 65), // Java 21 at least, Java 21 Recommended.
    V26_1(Pattern.compile("^26\\.[1-9]\\d?"), 69, 69), // Up to Java 25

    ;

    companion object{
        fun get(s: MCJEServer): MCEnvCompMode{
            entries.forEach { mode ->
                if(mode.pattern.matcher(s.msi.version).matches()){
                    return mode
                }
            }
            return UNKNOWN
        }

        fun getCompatibilityMap(): List<MCEnvCompMode>{
            val list = mutableListOf<MCEnvCompMode>()
            EnvPool.getEnvList().forEach { env ->
                entries.forEach { mode ->
                    if(env.getVersion().code > mode.leastCode){
                        list.add(mode)
                    }
                }
            }
            return list
        }

        fun isRecommendEnv(ms: MCJEServer): Boolean = ms.msi.env.getVersion().code == get(ms).recommendCode

        fun hasRecommendEnv(ms: MCJEServer): Boolean = EnvPool.getEnvList().map { env -> env.getVersion().code }.contains(get(ms).recommendCode)

        fun adjustToRecommendEnv(ms: MCJEServer){
            if(hasRecommendEnv(ms)){
                val env = EnvPool.getEnvList().find { env -> env.getVersion().code == get(ms).recommendCode }
                if(env != null){
                    ms.msi.env = env
                }
            }
        }
    }
}