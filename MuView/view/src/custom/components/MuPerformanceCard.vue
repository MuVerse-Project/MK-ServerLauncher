<script setup lang="ts">
import {onMounted, onUnmounted, ref, shallowRef} from "vue";
import {useTransition} from "@vueuse/core";
import {MuWSConnection} from "@/api/MuCoreConnector.ts";
import {Card, CardContent, CardHeader, CardTitle} from "@shadcn/card";
import {Skeleton} from "@shadcn/skeleton";
import {Activity, CircleStop, Cpu, MemoryStick, Server, MonitorCog} from "@lucide/vue";
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

let OnlineServerCountAnime = useTransition(OnlineServerCount, { duration: 1500 })
let StoppedServerCountAnime = useTransition(StoppedServerCount, { duration: 1500 })
let TotalServerCountAnime = useTransition(TotalServerCount, { duration: 1500 })
</script>

<template>
  <div class="flex w-full min-w-0 flex-col gap-5">
    <!-- 顶排指标卡：手机上两列、平板三列、宽屏五列 -->
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-5">
      <!-- 服务器概览 -->
      <div class="flex items-center gap-3 rounded-xl border bg-card p-4">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-fuchsia-600/10 text-fuchsia-600">
          <Server class="size-5" />
        </div>
        <div class="flex min-w-0 flex-col">
          <span class="text-xs text-muted-foreground">{{ $t("muPerformanceCard.server.total") }}</span>
          <span class="truncate text-lg font-semibold">
            <skeleton v-if="onLoading" class="inline-block h-5 w-8 align-middle" />
            <template v-else>{{ TotalServerCountAnime }}</template>
          </span>
        </div>
      </div>

      <!-- 正在运行 -->
      <div class="flex items-center gap-3 rounded-xl border bg-card p-4">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600">
          <Activity class="size-5" />
        </div>
        <div class="flex min-w-0 flex-col">
          <span class="text-xs text-muted-foreground">{{ $t("muPerformanceCard.server.online") }}</span>
          <span class="truncate text-lg font-semibold">
            <skeleton v-if="onLoading" class="inline-block h-5 w-8 align-middle" />
            <template v-else>{{ OnlineServerCountAnime }}</template>
          </span>
        </div>
      </div>

      <!-- 已停止 -->
      <div class="flex items-center gap-3 rounded-xl border bg-card p-4">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
          <CircleStop class="size-5" />
        </div>
        <div class="flex min-w-0 flex-col">
          <span class="text-xs text-muted-foreground">{{ $t("muPerformanceCard.server.stopped") }}</span>
          <span class="truncate text-lg font-semibold">
            <skeleton v-if="onLoading" class="inline-block h-5 w-8 align-middle" />
            <template v-else>{{ StoppedServerCountAnime }}</template>
          </span>
        </div>
      </div>

      <!-- CPU -->
      <div class="flex items-center gap-3 rounded-xl border bg-card p-4">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-fuchsia-600/10 text-fuchsia-600">
          <Cpu class="size-5" />
        </div>
        <div class="flex min-w-0 flex-col">
          <span class="text-xs text-muted-foreground">{{ $t("muPerformanceCard.performance.cpu") }}</span>
          <span class="whitespace-nowrap text-lg font-semibold">
            <skeleton v-if="onLoading" class="inline-block h-5 w-12 align-middle" />
            <template v-else>{{ CpuUsage.toFixed(2) }}%</template>
          </span>
        </div>
      </div>

      <!-- 内存 -->
      <div class="flex items-center gap-3 rounded-xl border bg-card p-4">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-full bg-sky-500/10 text-sky-600">
          <MemoryStick class="size-5" />
        </div>
        <div class="flex min-w-0 flex-col">
          <span class="text-xs text-muted-foreground">{{ $t("muPerformanceCard.performance.mem") }}</span>
          <span class="whitespace-nowrap text-lg font-semibold">
            <skeleton v-if="onLoading" class="inline-block h-5 w-12 align-middle" />
            <template v-else>{{ MemUsage.toFixed(2) }}%</template>
          </span>
        </div>
      </div>
    </div>

    <!-- 下方两块：宽屏并排，窄屏堆叠 -->
    <div class="grid grid-cols-1 gap-5 xl:grid-cols-2">
      <!-- 服务器状态 -->
      <card class="min-w-0">
        <card-header>
          <card-title class="flex items-center gap-2 text-base">
            <Server class="size-4 text-muted-foreground" />
            {{ $t("muPerformanceCard.server.title") }}
          </card-title>
        </card-header>
        <card-content>
          <div class="flex flex-col divide-y">
            <div class="flex items-center justify-between py-3 first:pt-0">
              <span class="text-sm">{{ $t("muPerformanceCard.server.online") }}</span>
              <span class="whitespace-nowrap text-sm font-semibold">
                <skeleton v-if="onLoading" class="inline-block h-4 w-8 align-middle" />
                <template v-else>{{ OnlineServerCount }}</template>
              </span>
            </div>
            <div class="flex items-center justify-between py-3">
              <span class="text-sm">{{ $t("muPerformanceCard.server.stopped") }}</span>
              <span class="whitespace-nowrap text-sm font-semibold">
                <skeleton v-if="onLoading" class="inline-block h-4 w-8 align-middle" />
                <template v-else>{{ StoppedServerCount }}</template>
              </span>
            </div>
            <div class="flex items-center justify-between py-3 last:pb-0">
              <span class="text-sm">{{ $t("muPerformanceCard.server.total") }}</span>
              <span class="whitespace-nowrap text-sm font-semibold">
                <skeleton v-if="onLoading" class="inline-block h-4 w-8 align-middle" />
                <template v-else>{{ TotalServerCount }}</template>
              </span>
            </div>
          </div>
        </card-content>
      </card>

      <!-- 应用信息 -->
      <card class="min-w-0">
        <card-header>
          <card-title class="flex items-center gap-2 text-base">
            <MonitorCog class="size-4 text-muted-foreground" />
            {{ $t("muPerformanceCard.appInfo.title") }}
          </card-title>
        </card-header>
        <card-content>
          <div class="flex flex-col divide-y">
            <div class="flex items-center justify-between gap-4 py-3 first:pt-0">
              <span class="shrink-0 text-sm">{{ $t("muPerformanceCard.appInfo.coreLabel") }}</span>
              <span class="truncate text-sm font-semibold">
                <skeleton v-if="onLoading" class="inline-block h-4 w-24 align-middle" />
                <template v-else>{{ MuCoreName }}</template>
              </span>
            </div>
            <div class="flex items-center justify-between gap-4 py-3 last:pb-0">
              <span class="shrink-0 text-sm">{{ $t("muPerformanceCard.appInfo.versionLabel") }}</span>
              <span class="truncate text-sm font-semibold">
                <skeleton v-if="onLoading" class="inline-block h-4 w-24 align-middle" />
                <template v-else>{{ MuCoreVer }}</template>
              </span>
            </div>
          </div>
        </card-content>
      </card>
    </div>
  </div>
</template>
