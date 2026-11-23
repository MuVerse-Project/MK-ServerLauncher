<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {Button} from "@shadcn/button";
import {MuWSConnection} from "@/api/MuCoreConnector.ts";
import {
  MuServerList,
  refreshMuServerList,
  ServerStatusCode,
  startMuServer,
  statusColor,
  statusLabel,
  stopMuServer,
} from "@view/muserver/server.ts";

const route = useRoute();
const router = useRouter();
const msid = String(route.params.msid || "");

const server = computed(() => MuServerList.value.find((s) => s.msi?.msid === msid));

interface LogLine {
  lvl: string;
  msg: string;
  time: string;
}

const logs = ref<LogLine[]>([]);
const logBox = ref<HTMLElement | null>(null);
const follow = ref(true);

let ws: ReturnType<typeof MuWSConnection> | null = null;
let tsk = -1;

const pushLog = (lvl: string, msg: string) => {
  const d = new Date();
  const time = [d.getHours(), d.getMinutes(), d.getSeconds()]
      .map((n) => String(n).padStart(2, "0"))
      .join(":");
  logs.value.push({lvl, msg, time});
  if (logs.value.length > 1000) {
    logs.value.splice(0, logs.value.length - 1000);
  }
};

const scrollToBottom = async () => {
  if (!follow.value) return;
  await nextTick();
  if (logBox.value) logBox.value.scrollTop = logBox.value.scrollHeight;
};

const poll = () => {
  if (!ws) return;
  const raw: any = ws.getMsg();
  if (!raw) return;
  const data = raw.MP_DATA;
  if (!data) return;
  const op = data.MS_OP;
  if (!op) return;
  if (typeof op.MSS === "number") {
    if (server.value) server.value.mss = op.MSS;
  } else if (typeof op.msg === "string") {
    pushLog(String(op.lvl ?? "INFO"), op.msg);
    scrollToBottom();
  }
};

const isRunning = computed(() => {
  const c = server.value?.mss;
  return c === ServerStatusCode.RUNNING || c === ServerStatusCode.STARTING;
});
const isStopped = computed(() => server.value?.mss === ServerStatusCode.STOPPED);

const onStart = () => startMuServer(msid);
const onStop = () => stopMuServer(msid, false);
const onForceStop = () => stopMuServer(msid, true);
const goBack = () => router.push("/server");

onMounted(async () => {
  await refreshMuServerList();
  ws = MuWSConnection(`api/v1/server/${encodeURIComponent(msid)}`);
  tsk = window.setInterval(poll, 500);
});

onUnmounted(() => {
  if (tsk !== -1) {
    clearInterval(tsk);
    tsk = -1;
  }
  ws?.close();
  ws = null;
});
</script>

<template>
  <div class="flex flex-col gap-5 w-full">
    <div class="flex items-center gap-3 flex-wrap">
      <Button variant="ghost" size="sm" @click="goBack">← 返回列表</Button>
      <blockquote class="border-l-2 border-black dark:border-white pl-3 font-bold">
        {{ server?.msi?.name || msid }}
      </blockquote>
      <span v-if="server" :class="['font-medium', statusColor(server.mss)]">
        {{ statusLabel(server.mss) }}
      </span>
      <div class="ml-auto flex gap-2">
        <Button size="sm" variant="outline" :disabled="!isStopped" @click="onStart">启动</Button>
        <Button size="sm" variant="outline" :disabled="!isRunning" @click="onStop">停止</Button>
        <Button size="sm" variant="ghost" @click="onForceStop">强制关闭</Button>
      </div>
    </div>

    <div v-if="server" class="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
      <div class="rounded-md border p-3">
        <div class="text-xs text-muted-foreground">版本</div>
        <div class="font-medium">{{ server.msi.version }}</div>
      </div>
      <div class="rounded-md border p-3">
        <div class="text-xs text-muted-foreground">端口</div>
        <div class="font-medium">{{ server.msi.port }}</div>
      </div>
      <div class="rounded-md border p-3">
        <div class="text-xs text-muted-foreground">Java 环境</div>
        <div class="font-medium">{{ server.msi.env?.name || "-" }}</div>
      </div>
      <div class="rounded-md border p-3">
        <div class="text-xs text-muted-foreground">内存</div>
        <div class="font-medium">
          <template v-if="server.mssc">{{ server.mssc.minMemory }} - {{ server.mssc.maxMemory }} MB</template>
          <template v-else>-</template>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-2">
      <div class="flex items-center gap-3">
        <span class="font-bold">日志</span>
        <label class="flex items-center gap-1 text-sm text-muted-foreground">
          <input v-model="follow" type="checkbox"/> 跟随
        </label>
        <Button class="ml-auto" size="sm" variant="outline" @click="logs = []">清屏</Button>
      </div>
      <pre
          ref="logBox"
          class="h-[420px] overflow-auto rounded-md border bg-muted/30 p-3 text-xs whitespace-pre-wrap break-all"
      ><template v-if="logs.length === 0">（暂无日志）</template><template v-for="(l, i) in logs" :key="i"><span class="text-muted-foreground">{{ l.time }}</span><span :class="{'text-red-600': l.lvl === 'ERROR', 'text-amber-600': l.lvl === 'WARN'}"> [{{ l.lvl }}] </span><span>{{ l.msg }}</span>
</template></pre>
    </div>
  </div>
</template>
