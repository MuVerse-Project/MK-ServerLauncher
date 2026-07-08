package me.mucloud.application.mk.serverlauncher.muenv

/**
 * # | Java Version
 *
 * - Used for marking Java Development Edition Version and class Version.
 * - Used to determine whether this JDK version can be used in a specific Minecraft Dedicated Server.
 *
 * @since TinyNova V1 | DEV.1
 * @author Mu_Cloud
 */
enum class JavaVersion(
    val jVer: String,
    val code: Int,
){
    UNKNOWN("unknown", 0),
    V1_8("1.8", 52),
    V9("9", 53),
    V10("10", 54),
    V11("11", 55),
    V12("12", 56),
    V13("13", 57),
    V14("14", 58),
    V15("15", 59),
    V16("16", 60),
    V17("17", 61),
    V18("18", 62),
    V19("19", 63),
    V20("20", 64),
    V21("21", 65),
    V22("22", 66),
    V23("23", 67),
    V24("24", 68),
    V25("25", 69),
    V26("26", 70),
    ;

    override fun toString(): String = code.toString()

    companion object{
        fun get(env: JavaEnvironment): JavaVersion =
            entries.find { v -> env.getVersionString().startsWith(v.jVer) } ?: UNKNOWN

        fun get(code: Int): JavaVersion = entries.find { it.code == code } ?: UNKNOWN

        fun final() = entries.last()
    }
}