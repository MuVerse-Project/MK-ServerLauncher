package me.mucloud.application.mk.serverlauncher.mupacket.api

interface ExecutableMuPacket: MuPacket {
    fun execute(): MuPacket
}