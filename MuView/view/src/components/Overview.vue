<script setup lang="ts">
import {onMounted, onUnmounted, ref, shallowRef} from "vue";
import {useTransition} from "@vueuse/core";
import {MuWebSocket} from "@api/MuCoreConnector";

let tsk = -1
let onLoading = ref(true)
let ws: MuWebSocket
onMounted(() => {
  ws = new MuWebSocket("overview")
  let wsMsg = ref()
  if(tsk === -1){
    tsk = setInterval(() => {
      if(ws.getMsg() != undefined){
        wsMsg = ws.getMsg()
        console.log(wsMsg)
        processCoreData(wsMsg)
      }
    }, 1000)
  }
})

onUnmounted(() => {
  console.log("Call Cancelled")
  if(tsk != -1){
    clearInterval(tsk)
    tsk = -1
  }
  ws.close()
})

let CpuUsage = ref(0)
let MemUsage = ref(0)

let OnlineServerCount = shallowRef(0)
let StoppedServerCount = shallowRef(0)
let TotalServerCount = shallowRef(0)

let MuCoreName = ref("")
let MuCoreVer = ref("")
let MuPluginCount = ref("")
let MuTemplatePackCount = ref("")

function processCoreData(msg: any){
  let systemStatus = msg.systemStatus
  let serverStatus = msg.serverStatus
  let appinfo = msg.appInfoStatus

  CpuUsage.value = systemStatus.CpuUsage
  MemUsage.value = systemStatus.MemUsage

  OnlineServerCount.value = serverStatus.onlineServer
  StoppedServerCount.value = serverStatus.offlineServer
  TotalServerCount.value = serverStatus.totalServer

  MuCoreName.value = appinfo.core
  MuCoreVer.value = appinfo.ver
  MuPluginCount.value = appinfo.pluginCount
  MuTemplatePackCount.value = appinfo.templatePackCount
  onLoading.value = false
}

let OnlineServerCountAnime = useTransition(
    OnlineServerCount,
    { duration: 1500 }
)
let StoppedServerCountAnime = useTransition(
    StoppedServerCount,
    { duration: 1500 }
)
let TotalServerCountAnime = useTransition(
    TotalServerCount,
    { duration: 1500 }
)

// Watching Viewport Width to switch Columns Number
let cols = ref("")
addEventListener("resize", () => {
  if(window.innerWidth < 768){
    cols.value = ""
  }else{
    cols.value = "grid-cols-2"
  }
})

</script>

<template>
  <el-space :size="20" wrap fill fill-ratio="40" class="w-full">
    <el-card class="h-55">
      <template #header>Overview</template>
      <div class="flex flex-row gap-5 justify-around items-center">
        <el-progress type="dashboard" :percentage="Number.parseFloat(CpuUsage.toFixed(2))">
          <template #default="{ percentage }">
            <span class="block text-xl">{{percentage}}%</span>
            <span class="block text-base">CPU</span>
          </template>
        </el-progress>
        <el-progress type="dashboard" :percentage="Number.parseFloat(MemUsage.toFixed(2))">
          <template #default="{ percentage }">
            <span class="block text-xl">{{percentage}}%</span>
            <span class="block text-base">Memory</span>
          </template>
        </el-progress>
      </div>
    </el-card >
    <el-card class="h-55" body-class="h-2/3">
      <template #header>Servers</template>
      <div class="flex flex-row gap-6 w-full justify-around items-center h-full">
        <div class="text-center text-lg">
          <el-statistic :value="OnlineServerCountAnime" class="mb-3"/>
          <span>Online</span>
        </div>
        <div class="text-center text-lg">
          <el-statistic :value="StoppedServerCountAnime" class="mb-3"/>
          <span>Stopped</span>
        </div>
        <div class="text-center text-lg">
          <el-statistic :value="TotalServerCountAnime" class="mb-3"/>
          <span>Total</span>
        </div>
      </div>
    </el-card>
    <el-card class="h-55">
      <template #header>Core Info</template>
      <el-skeleton :loading="onLoading" animated>
        <template #template>
          <div class="flex flex-row items-center"><span class="font-bold">Core:</span> <el-skeleton-item variant="text" class="ml-2" style="width: 30%"/></div>
          <div class="flex flex-row items-center"><span class="font-bold">Version:</span> <el-skeleton-item variant="text" class="ml-2" style="width: 30%"/></div>
          <div class="flex flex-row items-center"><span class="font-bold">Plugin Count:</span> <el-skeleton-item variant="text" class="ml-2" style="width: 30%"/></div>
          <div class="flex flex-row items-center"><span class="font-bold">TemplatePack Count:</span> <el-skeleton-item variant="text" class="ml-2" style="width: 30%"/></div>
        </template>
        <div><span class="font-bold">Core:</span> {{ MuCoreName }} </div>
        <div><span class="font-bold">Version:</span> {{ MuCoreVer }} </div>
        <div><span class="font-bold">Plugin Count:</span> {{ MuPluginCount }} </div>
        <div><span class="font-bold">TemplatePack Count:</span> {{ MuTemplatePackCount }} </div>
      </el-skeleton>
    </el-card>

  </el-space>
</template>

<style scoped>

</style>