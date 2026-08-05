export interface MuPacket<TData = unknown> {
    readonly MP_ID: string
    readonly MP_DATA: TData
}

export type MuPacketJSON<TData = unknown> = {
    MP_ID: string
    MP_DATA: TData
}

export type MuPacketDataValidator<TData> = (data: unknown) => data is TData

export interface MuPacketDefinition<
    TData = unknown,
    TPacket extends MuPacket<TData> = MuPacket<TData>,
> {
    readonly MP_ID: string
    readonly packetName?: string
    readonly isMP_DATA: MuPacketDataValidator<TData>
    readonly fromJSON: (data: TData, raw: MuPacketJSON<TData>) => TPacket
    readonly toJSON?: (packet: TPacket) => MuPacketJSON<TData>
}

export class MuPacketError extends Error {
    constructor(message: string) {
        super(message)
        this.name = new.target.name
    }
}

export class MuPacketRegistrationError extends MuPacketError {}

const MuPacketPool = new Map<string, MuPacketDefinition<unknown, MuPacket<unknown>>>()

export const isRecord = (value: unknown): value is Record<PropertyKey, unknown> => {
    return typeof value === "object" && value !== null
}

export const isMuPacketObject = (value: unknown): value is MuPacketJSON => {
    return isRecord(value)
        && typeof value.MP_ID === "string"
        && "MP_DATA" in value
}

export const reg = <TData, TPacket extends MuPacket<TData>>(
    definition: MuPacketDefinition<TData, TPacket>,
) => {
    if (!definition.MP_ID.trim()) {
        throw new MuPacketRegistrationError("MP_ID cannot be empty.")
    }

    if (MuPacketPool.has(definition.MP_ID)) {
        throw new MuPacketRegistrationError(`MP_ID already in use: ${definition.MP_ID}`)
    }

    MuPacketPool.set(
        definition.MP_ID,
        definition as unknown as MuPacketDefinition<unknown, MuPacket<unknown>>,
    )
    return definition
}

export const getMuPacketDefinition = (mpid: string) => {
    return MuPacketPool.get(mpid)
}

export const getMuPacketDefinitions = () => {
    return Array.from(MuPacketPool.values())
}

export const check = (mp: MuPacket | string) => {
    const mpid = typeof mp === "string" ? mp : mp.MP_ID
    return MuPacketPool.has(mpid)
}
