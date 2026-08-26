<script setup lang="ts">

import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@shadcn/dialog";
import {Button} from "@shadcn/button";
import {Plus} from "@lucide/vue";
import {Field, FieldGroup, FieldLabel} from "@shadcn/field";
import {Input} from "@shadcn/input";
import {ref} from "vue";
import {CreateMuEnvPacket} from "@/custom/api/MuEnvPacket.ts";
import {addMuEnv} from "@view/muenv/muenv.ts";

let ev_name = ref()
let ev_loc = ref()

const submit = () => {
  let mp = CreateMuEnvPacket.create({name: ev_name.value, path: ev_loc.value})
  addMuEnv(mp)
}

const clear = () => {
  ev_name.value = ''
  ev_loc.value = ''
}

</script>

<template>
  <Dialog>
    <DialogTrigger>
      <Button class="bg-green-600 hover:bg-green-700">
        <Plus/>
        {{ $t("muenv.importer.button") }}
      </Button>
    </DialogTrigger>
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{ $t("muenv.importer.title") }}</DialogTitle>
      </DialogHeader>
      <form>
        <FieldGroup>
          <Field>
            <FieldLabel for="ev_name">{{ $t("muenv.importer.name") }}</FieldLabel>
            <Input id="ev_name" type="text" v-model="ev_name"/>
          </Field>
          <Field>
            <FieldLabel for="ev_loc">{{ $t("muenv.importer.path") }}</FieldLabel>
            <Input id="ev_loc" type="text" v-model="ev_loc"/>
          </Field>
        </FieldGroup>
      </form>
      <DialogFooter>
        <DialogClose>
          <Button variant="outline" @click.capture="clear">{{ $t("muenv.importer.cancel") }}</Button>
        </DialogClose>
        <DialogClose>
          <Button class="bg-green-600 hover:bg-green-700" @click="submit()">{{ $t("muenv.importer.import") }}</Button>
        </DialogClose>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<style scoped>

</style>