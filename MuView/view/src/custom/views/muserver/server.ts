import {MuHttpClient} from "@/api/MuCoreConnector.ts";
import {type MuPacket} from "@/api/mupacket/MuPacket.ts";
import {MuMsgPacket} from "@/custom/api/MuMsgPacket.ts";
import {ref} from "vue";

/** 服务器状态码，与后端 ServerStatus 一一对应 */
export const ServerStatusCode = {
    CREATING: 0,
    STOPPED: 1,
    PREPARING: 2,
    RUNNING: 3,
    STOPPING: 4,
    RESTARTING: 5,
    IN_ERROR: 6,
    ERROR: 7,
    STARTING: 8,
} as const;

const STATUS_LABEL: Record<number, string> = {
    0: "创建中",
    1: "已停止",
    2: "准备中",
    3: "运行中",
    4: "停止中",
    5: "重启中",
    6: "异常重试",
    7: "错误",
    8: "启动中",
};

const STATUS_COLOR: Record<number, string> = {
    0: "text-muted-foreground",
    1: "text-muted-foreground",
    2: "text-amber-600",
    3: "text-emerald-600",
    4: "text-amber-600",
    5: "text-amber-600",
    6: "text-orange-600",
    7: "text-red-600",
    8: "text-amber-600",
};

export const statusLabel = (code?: number): string =>
    code === undefined ? "-" : (STATUS_LABEL[code] ?? "");

export const statusColor = (code?: number): string =>
    code === undefined ? "text-muted-foreground" : (STATUS_COLOR[code] ?? "text-muted-foreground");

/** 后端 MCJEServer 序列化后的形状（只取前端用得到的字段） */
export interface MuServer {
    msi: {
        msid: string;
        name: string;
        version: string;
        desc: string;
        port: number;
        env?: { name?: string; path?: string };
        type?: { id?: string; name?: string };
        msl?: string;
    };
    mssc?: {
        minMemory: number;
        maxMemory: number;
        hasGui: boolean;
        jvmFlag?: string;
    };
    mss?: number;
}

export const MuServerList = ref<MuServer[]>([]);

/** 拉取服务器列表；后端返回整份 MCJEServer 序列化数组 */
export const refreshMuServerList = () =>
    MuHttpClient.get<Object>("api/v1/server/list").then((r) => {
        const mpJson = r as MuPacket;
        MuServerList.value = ((mpJson.MP_DATA as unknown as MuServer[]) ?? []);
        return MuServerList.value;
    });

/** 启动服务器 */
export const startMuServer = (msid: string) =>
    MuHttpClient.get<MuMsgPacket>(`api/v1/server/start/${encodeURIComponent(msid)}`)
        .finally(() => refreshMuServerList());

/** 停止服务器；enforce=true 走 forcestop（强制关闭） */
export const stopMuServer = (msid: string, enforce = false) =>
     MuHttpClient.get<MuPacket>(
        `api/v1/server/${enforce ? "forcestop" : "stop"}/${encodeURIComponent(msid)}`
    ).finally(() => refreshMuServerList());

/** 删除服务器记录 */
export const deleteMuServer = (msid: string) =>
    MuHttpClient.get<MuMsgPacket>(`api/v1/server/delete/${encodeURIComponent(msid)}`)
        .finally(() => refreshMuServerList());
