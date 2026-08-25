import {MuPacket} from "@/api/mupacket/MuPacket.ts";
import {MuPacketRegistry} from "@/api/mupacket/MuPacketFactory.ts";

interface MuStatusPacket_MPDefinition{
    systemStatus: {
        CpuUsage: number
        MemUsage: number
    },
    serverStatus: {
        totalServer: number
        onlineServer: number
        offlineServer: number
    },
    appInfoStatus: {
        core: string
        ver: string
    }
}

export class MuStatusPacket extends MuPacket<MuStatusPacket_MPDefinition>{}

MuPacketRegistry.register("mucore.internal:status", MuStatusPacket)