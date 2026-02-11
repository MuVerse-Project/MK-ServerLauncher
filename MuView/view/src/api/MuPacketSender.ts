import {MuWebSocket} from "@api/MuCoreConnector";
import type {MuPacket} from "@api/MuPacket";

export class MuPacketSender {
    private readonly socket: MuWebSocket

    constructor(path: string) {
        this.socket = new MuWebSocket(path)
    }

    public send(packet: MuPacket): any {
        return this.socket.send(packet)
    }

    public close(): void {
        this.socket.close()
    }
}
