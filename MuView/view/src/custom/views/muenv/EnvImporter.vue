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
import {Plus, MoreHorizontal} from "@lucide/vue";
import {Field, FieldError, FieldGroup, FieldLabel} from "@shadcn/field";
import {Input} from "@shadcn/input";
import {reactive, ref, watch} from "vue";
import {CreateMuEnvPacket} from "@/custom/api/MuEnvPacket.ts";
import {addMuEnv} from "@view/muenv/muenv.ts";
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

let isOpen = ref(false)

let ev_name = ref('')
let ev_loc = ref('')

let ev_name_field_validate_result = reactive({isOk: true, msg: ""})

watch([ev_name, ev_loc], () => {
  let isOk = false;
  let msg = "";
  if(!ev_name.value.match("^[A-Za-z].*")){
    msg = t("muenv.importer.validator.ev_name.1")
  }else if(ev_name.value.length > 20){
    msg = t("muenv.importer.validator.ev_name.2")
  }else{
    isOk = true
  }
  ev_name_field_validate_result = {
    isOk: isOk,
    msg: msg
  }
})

const submit = () => {
  if(ev_name_field_validate_result.isOk){
    let mp = CreateMuEnvPacket.create({name: ev_name.value, path: ev_loc.value})
    addMuEnv(mp)
    isOpen.value = false
  }else{
    isOpen.value = true
  }
}

const clear = () => {
  ev_name.value = ''
  ev_loc.value = ''
}

</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogTrigger>
      <Button class="bg-green-600 hover:bg-green-700">
        <Plus/>
        {{ $t("muenv.importer.button") }}
      </Button>
    </DialogTrigger>
    <DialogContent @interactOutside="clear">
      <DialogHeader>
        <DialogTitle>{{ $t("muenv.importer.title") }}</DialogTitle>
      </DialogHeader>
      <form>
        <FieldGroup>
          <Field>
            <FieldLabel for="ev_name">{{ $t("muenv.importer.name") }}</FieldLabel>
            <Input id="ev_name" type="text" :aria-invalid="!ev_name_field_validate_result.isOk" v-model="ev_name" required/>
            <FieldError>{{ ev_name_field_validate_result.msg }}</FieldError>
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
        <Button class="bg-green-600 hover:bg-green-700" @click="submit()">{{ $t("muenv.importer.import") }}</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<style scoped>

</style>