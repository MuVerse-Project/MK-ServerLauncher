<script setup lang="ts">
import {computed} from "vue";
import {Button} from "@shadcn/button";
import {refreshMuServerList, MuServerList, statusLabel, statusColor, ServerStatusCode} from "@view/muserver/server.ts";
import ServerTableRowMenu from "@view/muserver/ServerTableRowMenu.vue";

refreshMuServerList();

const isRunning = (code?: number) =>
    code === ServerStatusCode.RUNNING || code === ServerStatusCode.STARTING;

const isStopped = (code?: number) => code === ServerStatusCode.STOPPED;

const total = computed(() => MuServerList.value.length);
const runningCount = computed(() => MuServerList.value.filter((s) => isRunning(s.mss)).length);
</script>

<template>
  <div class="flex flex-col gap-5 w-full">
    <blockquote class="border-l-2 border-black dark:border-white pl-3 font-bold">
      服务器管理
    </blockquote>

    <div class="flex items-center gap-3">
      <span class="text-sm text-muted-foreground">
        共 {{ total }} 个服务器，运行中 {{ runningCount }} 个
      </span>
      <Button class="ml-auto" size="sm" variant="outline" @click="refreshMuServerList()">刷新</Button>
    </div>

    <div class="w-full rounded-md border overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
        <tr class="border-b bg-muted/40">
          <th class="text-left px-3 py-2 font-medium whitespace-nowrap">名称</th>
          <th class="text-left px-3 py-2 font-medium whitespace-nowrap">版本</th>
          <th class="text-left px-3 py-2 font-medium whitespace-nowrap">端口</th>
          <th class="text-left px-3 py-2 font-medium whitespace-nowrap">状态</th>
          <th class="text-left px-3 py-2 font-medium whitespace-nowrap">内存</th>
          <th class="text-right px-3 py-2 font-medium whitespace-nowrap">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="MuServerList.length === 0">
          <td colspan="6" class="px-3 py-8 text-center text-muted-foreground">
            还没有服务器。先去创建一个，或在环境页确认 Java 环境已就绪。
                   </td>
        </tr>
        <tr
            v-for="s in MuServerList"
            :key="s.msi.msid"
            class="border-b last:border-b-0 hover:bg-muted/30"
        >
          <td class="px-3 py-2">
            <div class="font-medium">{{ s.msi.name }}</div>
            <div v-if="s.msi.desc" class="text-xs text-muted-foreground">{{ s.msi.desc }}</div>
                   </td>
          <td class="px-3 py-2 whitespace-nowrap">{{ s.msi.version }}</td>
          <td class="px-3 py-2 whitespace-nowrap">{{ s.msi.port }}</td>
          <td class="px-3 py-2 whitespace-nowrap">
            <span :class="['font-medium', statusColor(s.mss)]">{{ statusLabel(s.mss) }}</span>
          </td>
          <td class="px-3 py-2 whitespace-nowrap text-xs text-muted-foreground">
            <template v-if="s.mssc">{{ s.mssc.minMemory }} - {{ s.mssc.maxMemory }} MB</template>
                   </td>
          <td class="px-3 py-2 text-right">
            <ServerTableRowMenu
                :msid="s.msi.msid"
                :running="isRunning(s.mss)"
                :stopped="isStopped(s.mss)"
            />
                   </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
