/*
 * # MuPacket API For TypeScript | MuPacketAPI4TS #
 *
 * | TinyNova V0 |
 * |     1.0     |
 *
 * Author: MuMuBotV1 & Mu_Cloud
 */

/**
 * MuPacket Base
 *
 * Using in MuView-Frontend
 *
 * Structure:
 * {
 *     MP_ID: string,
 *     MP_DATA: T?
 * }
 */
export interface MuPacketData<T extends object = object> {
    MP_ID: string;
    MP_DATA: T;
    CID: number;
}

export abstract class MuPacket<T extends object = object> {
    readonly MP_ID: string;
    readonly MP_DATA: T;
    readonly CID: number;

    constructor(data: MuPacketData) {
        this.MP_ID = data.MP_ID;
        this.MP_DATA = data.MP_DATA as T;
        this.CID = data.CID;
    }

    toJSON(): MuPacketData {
        return {
            MP_ID: this.MP_ID,
            MP_DATA: this.MP_DATA,
            CID: this.CID,
        };
    }
}

export interface ExecutableMuPacket {
    execute(): void | Promise<void>;
}

/**
 * 解析失败 / 未注册的 MP_ID 用的错误包。
 *
 * 与 Kotlin 侧的 `mucore.internal:msg(ERR)` 对齐：宁可显式报错，
 * 也不要用匿名基类把故障伪装成「解析成功、字段全空」。
 */
export class MuPacketError extends MuPacket {
    readonly reason: string;

    constructor(data: MuPacketData, reason: string) {
        super(data);
        this.reason = reason;
    }

    get message(): string {
        return `MuPacketError: ${this.reason} (MP_ID=${this.MP_ID})`;
    }
}

export abstract class ExecutableMuPacketBase<T extends object = object>
    extends MuPacket<T>
    implements ExecutableMuPacket
{
    abstract execute(): void | Promise<void>;
}
