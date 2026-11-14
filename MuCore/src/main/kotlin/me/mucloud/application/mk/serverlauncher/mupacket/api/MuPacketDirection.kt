package me.mucloud.application.mk.serverlauncher.mupacket.api

/**
 * # 方向标记：MuCore → MuView
 *
 * 纯标记接口，不带任何成员。方向不再靠 `fromData` 抛异常来表达——
 * 发送端按接口在编译期挡住，接收端用 `is` 判断。
 *
 * 用法：包类与它的 `MuPacketInfo` 同时实现本接口，这样运行时也能靠 Info 判断方向。
 *
 * @since RainyZone V1 | DEV.6
 * @author Mu_Cloud
 */
interface MuPacket2View

/**
 * # 方向标记：MuView → MuCore
 *
 * 同上。双向包同时实现 [MuPacket2View] 与 [MuPacket2Core]，不另设第三接口。
 *
 * @since RainyZone V1 | DEV.6
 * @author Mu_Cloud
 */
interface MuPacket2Core
