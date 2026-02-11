import {ref} from "vue";
import {apiClient} from "@api/MuCoreConnector";

export let ENV_LIST = ref()

export const getEnvs = () => { apiClient.get("/api/v1/env/list").then(res => {
    ENV_LIST.value = res.data
})}

getEnvs()
