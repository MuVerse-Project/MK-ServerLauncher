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
import {Field, FieldError, FieldGroup, FieldLabel} from "@shadcn/field";
import {Input} from "@shadcn/input";
import {onMounted, ref} from "vue";
import {useI18n} from 'vue-i18n'
import {useForm, Field as VeeField} from "vee-validate";
import {InputGroup, InputGroupAddon, InputGroupInput, InputGroupText} from "@shadcn/input-group";
import {toTypedSchema} from "@vee-validate/zod";
import {z} from "zod";
import {addMuEnv} from "@view/muenv/muenv.ts";
import {CreateMuEnvPacket} from "@/custom/api/MuEnvPacket.ts";

const {t} = useI18n()

let isOpen = ref(false)

const MuEnvSchema = z.object({
  EV_NAME: z
      .string()
      .min(4, t("muenv.importer.validator.ev_name.2"))
      .max(20, t("muenv.importer.validator.ev_name.2"))
      .regex(new RegExp("^[A-Za-z].*"), t("muenv.importer.validator.ev_name.1")),
  EV_LOC: z
      .string()
      .nonempty(t("muenv.importer.validator.ev_loc.1")),
})

const { handleSubmit, resetForm, values } = useForm({
  validationSchema: toTypedSchema(MuEnvSchema),
  initialValues: {
    EV_NAME: '',
    EV_LOC: '',
  }
})

const onSubmit = handleSubmit((values) => {
  let rawMP = CreateMuEnvPacket.create({
    name: values.EV_NAME,
    path: values.EV_LOC,
  })
  addMuEnv(rawMP)
})

</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogTrigger>
      <Button class="bg-green-600 hover:bg-green-700">
        <Plus/>
        {{ $t("muenv.importer.button") }}
      </Button>
    </DialogTrigger>
    <DialogContent @interactOutside="resetForm()">
      <DialogHeader>
        <DialogTitle>{{ t("muenv.importer.title") }}</DialogTitle>
      </DialogHeader>
      <form @submit="onSubmit">
        <FieldGroup>
          <VeeField v-slot="{ componentField, value, errors }" name="EV_NAME" validateOnBlur>
            <Field :data-invaild="!!errors.length">
              <FieldLabel for="ev_name">{{ t("muenv.importer.name") }}</FieldLabel>
              <InputGroup>
                <InputGroupInput
                    id="ev_name"
                    type="text"
                    :aria-invalid="!!errors.length"
                    v-bind="componentField"
                />
                <InputGroupAddon align="inline-end">
                  <InputGroupText>
                    {{ value?.length || 0 }}/20
                  </InputGroupText>
                </InputGroupAddon>
              </InputGroup>
              <FieldError v-if="errors.length" :errors="errors"/>
            </Field>
          </VeeField>
          <VeeField v-slot="{ componentField, errors }" name="EV_LOC" validateOnBlur>
            <Field :data-invaild="!!errors.length">
              <FieldLabel for="ev_loc">{{ t("muenv.importer.path") }}</FieldLabel>
              <Input
                  id="ev_loc"
                  type="text"
                  :aria-invalid="!!errors.length"
                  v-bind="componentField"
              />
              <FieldError v-if="errors.length" :errors="errors"/>
            </Field>
          </VeeField>
        </FieldGroup>
      </form>
      <DialogFooter>
        <DialogClose>
          <Button variant="outline" @click.capture="resetForm()">{{ t("muenv.importer.cancel") }}</Button>
        </DialogClose>
        <DialogClose>
          <Button class="bg-green-600 hover:bg-green-700" type="submit">{{ t("muenv.importer.import") }}</Button>
        </DialogClose>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<style scoped>

</style>