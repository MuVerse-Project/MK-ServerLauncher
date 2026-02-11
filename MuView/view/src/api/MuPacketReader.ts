import type {MuPacket} from "@api/MuPacket";

export const parseMuPacket = (raw: string | object): MuPacket => {
    if (typeof raw === "string") {
        return JSON.parse(raw) as MuPacket
    }

    return raw as MuPacket
}
