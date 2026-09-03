import {MuHttpClient} from "@/api/MuCoreConnector.ts";
import {type MuPacket} from "@/api/mupacket/MuPacket.ts";
import {CreateMuEnvPacket} from "@/custom/api/MuEnvPacket.ts";
import {MuMsgPacket} from "@/custom/api/MuMsgPacket.ts";
import {ref} from "vue";
import {defineStore} from "pinia";

export interface MuEnv{
    EV_NAME: string,
    EV_VER: string,
    EV_CODE: number,
    EV_LOC: string,
}

export const refreshMuEnvList = () =>
    MuHttpClient.get<Object>("api/v1/env/list").then(r => {
        let mpJson = r as MuPacket
        MuEnvList.value = mpJson.MP_DATA as MuEnv[]
    })

export const addMuEnv = (mp: CreateMuEnvPacket) => {
    MuHttpClient.post<MuMsgPacket>("api/v1/env/create", mp)
        .then(r => r.execute())
        .finally(() => refreshMuEnvList())
}

export const delMuEnv = (name: string) => {
    MuHttpClient.get<MuMsgPacket>(`api/v1/env/delete/${name}`)
        .then(r => r.execute())
        .finally(() => refreshMuEnvList())
}

export const MuEnvList = ref<MuEnv[]>([])

export const useEnvImporterKey = defineStore('envImporterKey', () => {
    let EnvImporterKey = ref(0)
    const refreshKey = () => {
        EnvImporterKey.value++
    }
    return {EnvImporterKey, refreshKey}
})