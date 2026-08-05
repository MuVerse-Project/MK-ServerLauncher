import {
    getMuPacketDefinition,
    isMuPacketObject,
    type MuPacket,
    MuPacketError,
    type MuPacketJSON,
} from "@/api/mupacket/MuPacket.ts"

export class MuPacketReadError extends MuPacketError {
    public readonly raw: unknown

    constructor(message: string, raw?: unknown) {
        super(message)
        this.raw = raw
    }
}

export type MuPacketReadResult<TPacket extends MuPacket = MuPacket> =
    | {
        ok: true
        packet: TPacket
        raw: MuPacketJSON
    }
    | {
        ok: false
        error: MuPacketReadError
    }

type NormalizedMuPacket = {
    raw: MuPacketJSON
    definition: NonNullable<ReturnType<typeof getMuPacketDefinition>>
}

export class MuPacketReader {
    public static parseJSON(raw: unknown): unknown {
        if (typeof raw !== "string") {
            return raw
        }

        try {
            return JSON.parse(raw)
        } catch {
            throw new MuPacketReadError(`Unable to parse MuPacket JSON: ${raw}`, raw)
        }
    }

    public static isPacketShape(raw: unknown): raw is MuPacketJSON {
        try {
            return isMuPacketObject(MuPacketReader.parseJSON(raw))
        } catch {
            return false
        }
    }

    public static getDefinition(mpid: string) {
        return getMuPacketDefinition(mpid)
    }

    public static assertValid(raw: unknown): MuPacketJSON {
        return MuPacketReader.normalize(raw).raw
    }

    public static check(raw: unknown): boolean {
        return MuPacketReader.readSafely(raw).ok
    }

    public static readSafely<TPacket extends MuPacket = MuPacket>(
        raw: unknown,
    ): MuPacketReadResult<TPacket> {
        try {
            return {
                ok: true,
                ...MuPacketReader.readWithDefinition<TPacket>(raw),
            }
        } catch (error) {
            return {
                ok: false,
                error: error instanceof MuPacketReadError
                    ? error
                    : new MuPacketReadError(error instanceof Error ? error.message : String(error), raw),
            }
        }
    }

    public static read<TPacket extends MuPacket = MuPacket>(raw: unknown): TPacket {
        return MuPacketReader.readWithDefinition<TPacket>(raw).packet
    }

    private static readWithDefinition<TPacket extends MuPacket = MuPacket>(
        raw: unknown,
    ): { packet: TPacket, raw: MuPacketJSON } {
        const normalized = MuPacketReader.normalize(raw)
        const packet = normalized.definition.fromJSON(
            normalized.raw.MP_DATA,
            normalized.raw,
        ) as TPacket

        if (packet.MP_ID !== normalized.raw.MP_ID) {
            throw new MuPacketReadError(
                `MuPacket implementation returned mismatched MP_ID: ${packet.MP_ID}`,
                normalized.raw,
            )
        }

        return {
            packet,
            raw: normalized.raw,
        }
    }

    private static normalize(raw: unknown): NormalizedMuPacket {
        const parsed = MuPacketReader.parseJSON(raw)

        if (!isMuPacketObject(parsed)) {
            throw new MuPacketReadError("Value is not a MuPacket JSON object.", parsed)
        }

        const definition = getMuPacketDefinition(parsed.MP_ID)
        if (definition == null) {
            throw new MuPacketReadError(
                `No MuPacket implementation registered for MP_ID: ${parsed.MP_ID}`,
                parsed,
            )
        }

        if (!definition.isMP_DATA(parsed.MP_DATA)) {
            throw new MuPacketReadError(
                `Invalid MP_DATA for MP_ID: ${parsed.MP_ID}`,
                parsed,
            )
        }

        return {
            raw: parsed,
            definition,
        }
    }
}
