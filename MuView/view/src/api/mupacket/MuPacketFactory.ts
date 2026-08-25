import {type ExecutableMuPacket, MuPacket, type MuPacketData} from "@muapi/mupacket/MuPacket.ts";

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
            console.warn(`未注册的 MP_ID: ${data.MP_ID}，回退到基础 MuPacket`);
            // 动态创建一个匿名基础类
            return new (class extends MuPacket {})(data);
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

    static generateCID(): number {
        return Date.now() * 1000 + Math.floor(Math.random() * 1000);
    }
}