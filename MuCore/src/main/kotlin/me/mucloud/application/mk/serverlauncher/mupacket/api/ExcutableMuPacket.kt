package me.mucloud.application.mk.serverlauncher.mupacket.api

interface ExcutableMuPacket: MuPacket {
    fun excute(): MuPacket
}