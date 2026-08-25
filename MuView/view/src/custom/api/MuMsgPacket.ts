import {type ExecutableMuPacket, ExecutableMuPacketBase} from "@/api/mupacket/MuPacket.ts";
import {toast} from "vue-sonner";
import {h} from "vue";
import {Info} from "@lucide/vue";
import {MuPacketRegistry} from "@/api/mupacket/MuPacketFactory.ts";

/**
 * # MuPacket API For TypeScript | MuPacketAPI4TS
 *
 * ## MuView Packs - MuView Message MuPacket
 *
 * MP_ID: mucore.internal:msg
 *
 * MP_DATA:
 * ```
 * {
 *     type: "OK" | "WARN" | "TIP" | "ERROR" | "INFO"
 *     msg: string
 * }
 * ```
 */
interface MuMsgPacket_MPDefinition{
    status: "OK" | "WARN" | "TIP" | "ERROR" | "INFO"
    msg: string
}

export class MuMsgPacket extends ExecutableMuPacketBase<MuMsgPacket_MPDefinition>{
    static create(data: MuMsgPacket_MPDefinition){
        return new MuMsgPacket({
            MP_ID: "mucore.internal:msg",
            MP_DATA: data,
            CID: MuPacketRegistry.generateCID(),
        })
    }

    execute(): void{
        const type = this.MP_DATA.status
        const msg = this.MP_DATA.msg
        toast.info("???")
        console.log(type, msg)
        if(type === "OK"){
            toast.success(msg)
        }else if(type === "INFO"){
            toast.info(msg)
        }else if(type === "WARN"){
            toast.warning(msg)
        }else if(type === "ERROR"){
            toast.error(msg)
        }else if(type === "TIP"){
            toast.custom(h("div", {class: "flex flex-row gap-5"}, [h(Info), h(msg)]))
        }
    }
}

MuPacketRegistry.register("mucore.internal:msg", MuMsgPacket)