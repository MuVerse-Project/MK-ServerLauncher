package me.mucloud.application.mk.serverlauncher.mupacket.api

/**
 * # MuPacket API
 *
 * ## ExecutableMuPacket
 *
 * 可执行包的标记：需要 MuCore 侧执行逻辑的包实现它，`execute` 的返回值也是一个 [MuPacket]。
 *
 * 它表达的是**能力**，不是**方向**——方向由 [MuPacket2View] / [MuPacket2Core] 表达。
 *
 * @since RainyZone V0 | DEV.4
 * @author Mu_Cloud
 */
interface ExecutableMuPacket: MuPacket {
    fun execute(): MuPacket
}
