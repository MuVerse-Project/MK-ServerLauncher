<script setup lang="ts">
import {onMounted, onUnmounted, ref, shallowRef} from "vue";
import {useTransition} from "@vueuse/core";
import {MuWSConnection} from "@/api/MuCoreConnector.ts";
import {Card, CardContent, CardHeader, CardTitle} from "@shadcn/card";
import {Skeleton} from "@shadcn/skeleton";
import MuDashboard from "@mucom/MuDashboard.vue";
import {MuStatusPacket} from "@/custom/api/MuStatusPacket.ts";

let tsk = -1
let onLoading = ref(true)
let ws = MuWSConnection("api/v1/overview")

onMounted(() => {
  let wsMsg
  if(tsk === -1){
    tsk = setInterval(() => {
      const rawMsg = ws.getMsg()
      if(rawMsg instanceof MuStatusPacket){
        wsMsg = rawMsg.MP_DATA
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
        {{ $t("muPerformanceCard.title") }}
      </card-title>
    </card-header>
    <card-content>
      <div class="flex flex-col gap-5">
        <div class="flex md:flex-row flex-col gap-20 justify-between">
          <div class="flex flex-col gap-y-5 h-full">
            <blockquote class="mt-6 border-l-2 border-black dark:border-white pl-3 font-bold">
              {{ $t("muPerformanceCard.server.title") }}
            </blockquote>
            <div class="flex flex-row gap-10 w-full justify-around items-center mx-5">
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ OnlineServerCountAnime }}</span>
                <span>{{ $t("muPerformanceCard.server.online") }}</span>
              </div>
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ StoppedServerCountAnime }}</span>
                <span>{{ $t("muPerformanceCard.server.stopped") }}</span>
              </div>
              <div class="flex flex-col text-center text-lg">
                <span class="mb-3">{{ TotalServerCount }}</span>
                <span>{{ $t("muPerformanceCard.server.total") }}</span>
              </div>
            </div>
          </div>
          <div class="flex flex-col gap-y-5 h-full w-120">
            <blockquote class="mt-6 border-l-2 border-black dark:border-white pl-3 font-bold">
              {{ $t("muPerformanceCard.performance.title") }}
            </blockquote>
            <div class="flex flex-row gap-2">
              <mu-dashboard :value="CpuUsage" :max="100" as="plain">
                <template #header>
                  {{ $t("muPerformanceCard.performance.cpu") }}
                </template>
                <template #footer="{ percent }">
                  {{ percent }}%
                </template>
              </mu-dashboard>
              <mu-dashboard :value="MemUsage" :max="100" as="plain">
                <template #header>
                  {{ $t("muPerformanceCard.performance.mem") }}
                </template>
                <template #footer="{ percent }">
                  {{ percent }}%
                </template>
              </mu-dashboard>
            </div>

          </div>
        </div>
        <div class="flex flex-col gap-y-5 h-full overflow-hidden">
          <blockquote class="mt-6 border-l-2 border-black dark:border-white pl-3 font-bold">
            {{ $t("muPerformanceCard.appInfo.title") }}
          </blockquote>
          <div class="flex flex-col w-full">
            <div class="flex flex-row items-center">
              <span class="font-bold">{{ $t("muPerformanceCard.appInfo.coreLabel") }}:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else class="ml-2">{{ MuCoreName }}</span>
            </div>
            <div class="flex flex-row items-center">
              <span class="font-bold">{{ $t("muPerformanceCard.appInfo.versionLabel") }}:</span>
              <skeleton v-if="onLoading" class="ml-2 h-5 w-20" />
              <span v-else class="ml-2">{{ MuCoreVer }}</span>
            </div>
          </div>
        </div>
      </div>
    </card-content>
  </card>
</template>

