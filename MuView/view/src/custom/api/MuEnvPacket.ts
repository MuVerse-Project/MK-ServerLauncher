import {MuPacket} from "@muapi/mupacket/MuPacket.ts";
import {MuPacketRegistry} from "@/api/mupacket/MuPacketFactory.ts";

/**
 * # MuPacket API For TypeScript | MuPacketAPI4TS
 *
 * ## MuView Packs - Create Environment MuPacket
 *
 * MP_ID: muview.muenv:create
 *
 * MP_DATA:
 * ```
 * {
 *     name: string
 *     path: string
 * }
 * ```
 */
interface CreateMuEnvPacket_MPDataDefinition{
    name: string
    path: string
}

export class CreateMuEnvPacket extends MuPacket<CreateMuEnvPacket_MPDataDefinition>{
    static create(data: CreateMuEnvPacket_MPDataDefinition){
        return new CreateMuEnvPacket({
            MP_ID: "muview.muenv:create",
            MP_DATA: data,
            CID: MuPacketRegistry.generateCID(),
        })
    }
}

MuPacketRegistry.register("muview.muenv:create", CreateMuEnvPacket)