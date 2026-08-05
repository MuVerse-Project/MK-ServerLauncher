import {isRecord, reg, type MuPacket} from "@/api/mupacket/MuPacket.ts"

export type MuMsgType = "ok" | "info" | "warn" | "err"

export interface MuMsgData {
    type: MuMsgType
    msg: string[]
}

const MU_MSG_PACKET_ID = "mupack.muview:msg"
const MU_MSG_TYPES = new Set<MuMsgType>(["ok", "info", "warn", "err"])

export class MuMsgPacket implements MuPacket<MuMsgData> {
    public static readonly MP_ID = MU_MSG_PACKET_ID
    public readonly MP_ID = MU_MSG_PACKET_ID
    public readonly MP_DATA: MuMsgData

    public constructor(type: MuMsgType, msg: string[]) {
        this.MP_DATA = {
            type,
            msg: [...msg],
        }
    }

    public static isMP_DATA(data: unknown): data is MuMsgData {
        return isRecord(data)
            && MU_MSG_TYPES.has(data.type as MuMsgType)
            && Array.isArray(data.msg)
            && data.msg.every((item) => typeof item === "string")
    }

    public static fromJSON(data: MuMsgData): MuMsgPacket {
        switch (data.type) {
            case "ok":
                return new MuMsgOKPacket(data.msg)
            case "info":
                return new MuMsgInfoPacket(data.msg)
            case "warn":
                return new MuMsgWarnPacket(data.msg)
            case "err":
                return new MuMsgErrPacket(data.msg)
        }
    }
}

export class MuMsgOKPacket extends MuMsgPacket {
    public constructor(msg: string[]) {
        super("ok", msg)
    }
}

export class MuMsgInfoPacket extends MuMsgPacket {
    public constructor(msg: string[]) {
        super("info", msg)
    }
}

export class MuMsgWarnPacket extends MuMsgPacket {
    public constructor(msg: string[]) {
        super("warn", msg)
    }
}

export class MuMsgErrPacket extends MuMsgPacket {
    public constructor(msg: string[]) {
        super("err", msg)
    }
}

export const MuMsgPacketDefinition = reg<MuMsgData, MuMsgPacket>({
    MP_ID: MU_MSG_PACKET_ID,
    packetName: "MuMsgPacket",
    isMP_DATA: MuMsgPacket.isMP_DATA,
    fromJSON: MuMsgPacket.fromJSON,
})
