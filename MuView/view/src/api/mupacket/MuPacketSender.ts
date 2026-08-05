import {
    getMuPacketDefinition,
    isMuPacketObject,
    type MuPacket,
    MuPacketError,
    type MuPacketJSON,
} from "@/api/mupacket/MuPacket.ts"
import {MuPacketReader} from "@/api/mupacket/MuPacketReader.ts"

export class MuPacketSendError extends MuPacketError {
    public readonly raw: unknown

    constructor(message: string, raw?: unknown) {
        super(message)
        this.raw = raw
    }
}

export class MuPacketSender {
    public static pack(packet: MuPacket | MuPacketJSON): MuPacketJSON {
        if (!isMuPacketObject(packet)) {
            throw new MuPacketSendError("Only MuPacket objects can be packed.", packet)
        }

        const definition = getMuPacketDefinition(packet.MP_ID)
        if (definition == null) {
            throw new MuPacketSendError(
                `No MuPacket implementation registered for MP_ID: ${packet.MP_ID}`,
                packet,
            )
        }

        const packed = definition.toJSON?.(packet as never) ?? {
            MP_ID: packet.MP_ID,
            MP_DATA: packet.MP_DATA,
        }

        const result = MuPacketReader.readSafely(packed)
        if (!result.ok) {
            throw new MuPacketSendError(result.error.message, packed)
        }

        return packed
    }

    public static packIfMuPacket(value: unknown): unknown {
        return isMuPacketObject(value) ? MuPacketSender.pack(value) : value
    }

    public static stringify(packet: MuPacket | MuPacketJSON): string {
        return JSON.stringify(MuPacketSender.pack(packet))
    }
}
