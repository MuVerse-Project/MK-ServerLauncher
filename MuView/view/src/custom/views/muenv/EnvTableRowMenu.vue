<script setup lang="ts">
import {Button} from "@shadcn/button";
import {Trash} from "@lucide/vue";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@shadcn/dialog";
import {delMuEnv} from "@view/muenv/muenv.ts";

const props = defineProps<{
  env: string
}>()

</script>

<template>
  <div class="flex flex-row gap-2 mx-10 justify-end">
    <Dialog>
      <DialogTrigger as-child>
        <Button variant="ghost" size="icon" class="text-red-600">
          <Trash/>
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {{ $t("muenv.deleter.title") }}
          </DialogTitle>
        </DialogHeader>
        <span>{{ $t("muenv.deleter.description") }}</span>
        <span class="text-sm text-gray-400">{{ $t("muenv.deleter.description2") }}</span>
        <DialogFooter>
          <DialogClose>
            <Button variant="outline">{{ $t("muenv.deleter.cancel") }}</Button>
          </DialogClose>
          <DialogClose>
            <Button variant="destructive" @click="delMuEnv(props.env)">{{ $t("muenv.deleter.confirm") }}</Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>

</style>