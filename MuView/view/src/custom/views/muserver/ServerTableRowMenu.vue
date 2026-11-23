<script setup lang="ts">
import {Button} from "@shadcn/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@shadcn/dialog";
import {deleteMuServer, startMuServer, stopMuServer} from "@view/muserver/server.ts";

const props = defineProps<{
  msid: string
  running: boolean
  stopped: boolean
}>()

const onStart = () => {
  if (!props.stopped) return
  startMuServer(props.msid)
}

const onStop = () => {
  if (!props.running) return
  stopMuServer(props.msid, false)
}

const onForceStop = () => {
  stopMuServer(props.msid, true)
}
</script>

<template>
  <div class="flex flex-row gap-2 justify-end items-center">
    <Button variant="outline" size="sm" :disabled="!props.stopped" @click="onStart">启动</Button>
    <Button variant="outline" size="sm" :disabled="!props.running" @click="onStop">停止</Button>
    <Button variant="ghost" size="sm" @click="onForceStop">强制关闭</Button>

    <Dialog>
      <DialogTrigger as-child>
        <Button variant="ghost" size="sm" class="text-red-600" :disabled="!props.stopped">
          删除
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>确认删除</DialogTitle>
        </DialogHeader>
        <span>确定要删除这个服务器吗？文件不会一并删掉，但记录会从列表里移除。</span>
        <DialogFooter>
          <DialogClose>
            <Button variant="outline">取消</Button>
          </DialogClose>
          <DialogClose>
            <Button variant="destructive" @click="deleteMuServer(props.msid)">删除</Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>

</style>
