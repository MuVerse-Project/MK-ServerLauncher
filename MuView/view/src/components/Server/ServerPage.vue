<script setup lang="ts">
import {MuWebSocket} from "@api/MuCoreConnector";
import {onMounted, ref} from "vue";
import type {TabPaneName} from "element-plus";
import {useRouter} from "vue-router";
import {usingServers} from "@api/MuServer";

const router = useRouter()
const props = defineProps({name: String})
onMounted(() => {
  if(usingServers.value.filter((name) => name === props.name).length === 0){
    usingServers.value.push(props.name as string)
  }
})
const currentUsing = ref(props.name)

const connectServer = (name: string) => {
  const ws = getServerConnection(name)
  if(ws.isConnect()){
    ws.send({
      type: "",

    })
  }else{
    ws.getMsg()
  }
}

const getServerConnection = (name: string): MuWebSocket => { return new MuWebSocket(`api/v1/server/${name}`) }

const handleRemove = (
    targetName: TabPaneName,
) => {
  let oriTab = usingServers.value
  if(currentUsing.value === targetName){
    oriTab.forEach((tab, index) => {
      if(tab === targetName){
        const nextTab = oriTab[index +1] || oriTab[index -1]
        if(nextTab){
          currentUsing.value = nextTab
        }
      }
    })
  }
  usingServers.value = oriTab.filter((name) => name !== targetName)
  if(usingServers.value.length === 0){
    router.push('/servermanager')
  }
}
</script>

<template>
  <el-space fill class="w-full">
    <el-tabs v-model="currentUsing" type="border-card" class="shadow-(--el-box-shadow)" @tab-remove="handleRemove" @tab-change="">
      <el-tab-pane v-for="i in usingServers" :label="i" :name="i" @load.capture="connectServer(i)" closable>
      </el-tab-pane>
    </el-tabs>
  </el-space>

</template>

<style scoped>

</style>