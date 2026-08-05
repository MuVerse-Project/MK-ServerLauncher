<script setup lang="ts">
import {onMounted, onUnmounted, ref, shallowRef} from "vue";
import {useTransition} from "@vueuse/core";
import {MuWSConnection} from "@muapi/MuCoreConnector";
import {Card, CardContent, CardHeader, CardTitle} from "@shadcn/card";
import {Skeleton} from "@shadcn/skeleton";

let tsk = -1
let onLoading = ref(true)
let ws = MuWSConnection("api/v1/overview")

onMounted(() => {
  let wsMsg
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
</script>

<template>
  <card>
    <card-header>
      <card-title>
        Overview
      </card-title>
    </card-header>
    <card-content>
      <div class="flex flex-col gap-5">
        <div class="flex flex-row gap-10">
          <div class="flex flex-col gap-y-5 h-full">
            <blockquote class="mt-6 border-l-2 border-black pl-3 font-bold">
              Server
            </blockquote>
            <div class="flex flex-row gap-5 w-full justify-around items-center">
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ OnlineServerCountAnime }}</span>
                <span>Online</span>
              </div>
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ StoppedServerCountAnime }}</span>
                <span>Stopped</span>
              </div>
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ TotalServerCount }}</span>
                <span>Total</span>
              </div>
            </div>
          </div>
          <div class="flex flex-col gap-y-5 h-full">
            <blockquote class="mt-6 border-l-2 border-black pl-3 font-bold">
              Performance
            </blockquote>
            <div class="flex flex-row gap-5 w-full justify-around items-center">
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ Number.parseFloat(CpuUsage.toFixed(2)) }}%</span>
                <span>CPU Usage%</span>
              </div>
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ Number.parseFloat(MemUsage.toFixed(2)) }}%</span>
                <span>MEM Usage%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="flex flex-col gap-y-5 h-full overflow-hidden">
          <blockquote class="mt-6 border-l-2 border-black pl-3 font-bold">
            MK-ServerLauncher APP Info
          </blockquote>
          <div class="flex flex-col w-full">
            <div class="flex flex-row items-center">
              <span class="font-bold">Core:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else>{{ MuCoreName }}</span>
            </div>
            <div class="flex flex-row items-center">
              <span class="font-bold">Version:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else>{{ MuCoreVer }}</span>
            </div>
            <div class="flex flex-row items-center">
              <span class="font-bold">Plugin Count:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else>0</span>
            </div>
            <div class="flex flex-row items-center">
              <span class="font-bold">TemplatePack Count:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else>0</span>
            </div>
          </div>
        </div>
      </div>
    </card-content>
  </card>
</template>

