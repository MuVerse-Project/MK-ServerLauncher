import {type ExecutableMuPacket, MuPacket, MuPacketError, type MuPacketData} from "@muapi/mupacket/MuPacket.ts";

type MuPacketConstructor<T extends MuPacket = MuPacket> = new (data: MuPacketData) => T;

export class MuPacketRegistry {
    private static readonly registry = new Map<string, MuPacketConstructor>();

    /** 注册 MP_ID 与对应类的映射 */
    static register(mpId: string, ctor: MuPacketConstructor): void {
        this.registry.set(mpId, ctor);
        console.log(`[MuPacketAPI4TS] Registered: ${mpId}`)
    }

    /** 根据 JSON 自动转换为对应类 */
    static parse(json: string | MuPacketData): MuPacket {
        const data = typeof json === "string" ? JSON.parse(json) : json;
        const ctor = this.registry.get(data.MP_ID);

        if (!ctor) {
            const reason = `未注册的 MP_ID: ${data.MP_ID}`;
            console.warn(`${reason}，返回 MuPacketError`);
            return new MuPacketError(data, reason);
        }

        // 方向自检：接口会被擦除，只能读静态字段。收到 2core 的包说明对面发错了方向。
        const direction = (ctor as unknown as { DIRECTION?: string }).DIRECTION;
        if (direction === "2core") {
            console.warn(`收到方向为 2core 的包：${data.MP_ID}，MuView 不该接收它`);
        }

        return new ctor(data);
    }

    /** 判断是否为 ExecutableMuPacket */
    static isExecutable(packet: MuPacket): packet is ExecutableMuPacket & MuPacket {
        return "execute" in packet && typeof (packet as ExecutableMuPacket).execute === "function";
    }

    /** 获取所有已注册的 MP_ID */
    static getRegisteredIds(): string[] {
        return Array.from(this.registry.keys());
    }

    private static cidSeq = 0;

    /** 进程内单调自增，与 Kotlin 侧对齐；不追求跨实例唯一。 */
    static generateCID(): number {
        return ++this.cidSeq;
    }
}