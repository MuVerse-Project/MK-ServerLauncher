export interface MuPacket {
    MP_ID: string
    MP_DATA: any
}

export interface PacketOperator {
    getID(): string
    setData(): Record<string, any>
    toJson(): Record<string, any>
    operate(): boolean
}

export abstract class AbstractMuPacket implements PacketOperator {
    abstract id: string
    abstract operation: string

    getID(): string {
        return this.id
    }

    setData(): Record<string, any> {
        return {}
    }

    toJson(): Record<string, any> {
        return {
            MP_ID: this.getID(),
            MP_DATA: this.setData(),
        }
    }

    operate(): boolean {
        return true
    }
}
