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
            Confirm
          </DialogTitle>
        </DialogHeader>
        <span>Surely to Delete this Environment? (Not delete file)</span>
        <span class="text-sm text-gray-400">If you want to re-register this Environment, you can using "Import" on Environment Page</span>
        <DialogFooter>
          <DialogClose>
            <Button variant="outline">Cancel</Button>
            <Button variant="destructive" @click="delMuEnv(props.env)">Confirm</Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>

</style>