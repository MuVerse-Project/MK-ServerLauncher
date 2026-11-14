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
 *     type: "OK" | "WARN" | "TIP" | "ERR" | "INFO"
 *     msg: string
 * }
 * ```
 */
interface MuMsgPacket_MPDefinition{
    /** 后端字段名：Kotlin 侧 MuMsgPacket.getData() 写的是 type */
    type?: "OK" | "WARN" | "TIP" | "ERR" | "INFO"
    /** 旧名字，只为兼容早期数据；新代码一律读 type */
    status?: "OK" | "WARN" | "TIP" | "ERR" | "INFO"
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
        // 后端发的是 type；以前这里读 status，字段永远取不到，于是 toast 一次都不弹
        const type = this.MP_DATA.type ?? this.MP_DATA.status
        const msg = this.MP_DATA.msg
        if(type === "OK"){
            toast.success(msg)
        }else if(type === "INFO"){
            toast.info(msg)
        }else if(type === "WARN"){
            toast.warning(msg)
        }else if(type === "ERR"){
            toast.error(msg)
        }else if(type === "TIP"){
            toast.custom(h("div", {class: "flex flex-row gap-5"}, [h(Info), h(msg)]))
        }
    }
}

MuPacketRegistry.register("mucore.internal:msg", MuMsgPacket)