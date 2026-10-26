<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import {useI18n} from "vue-i18n"
import {Button} from "@shadcn/button"
import {Input} from "@shadcn/input"
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@shadcn/card"
import {login} from "@muapi/session.ts"

/**
 * ## MuView 登录页
 *
 * 与主界面共用同一套 shadcn 组件、主题变量与 i18n,
 * 因此不会出现"后端渲染的门户"与前端风格割裂的问题。
 *
 * @since VoidLand V0 | DEV.1
 */
const {t} = useI18n()
const router = useRouter()

const token = ref("")
const submitting = ref(false)
const failed = ref(false)

async function submit() {
  if (!token.value.trim() || submitting.value) return
  submitting.value = true
  failed.value = false
  const ok = await login(token.value.trim())
  submitting.value = false
  if (ok) {
    await router.replace("/")
  } else {
    failed.value = true
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-background p-6">
    <Card class="w-full max-w-sm">
      <CardHeader>
        <CardTitle class="text-xl">{{ t("login.title") }}</CardTitle>
        <CardDescription class="leading-relaxed">{{ t("login.description") }}</CardDescription>
      </CardHeader>
      <CardContent>
        <form class="flex flex-col gap-4" @submit.prevent="submit">
          <Input
              v-model="token"
              type="password"
              autocomplete="current-password"
              :placeholder="t('login.placeholder')"
              :aria-invalid="failed"
              autofocus
          />
          <p v-if="failed" class="text-sm text-destructive">{{ t("login.failed") }}</p>
          <Button type="submit" class="w-full" :disabled="submitting || !token.trim()">
            {{ submitting ? t("login.submitting") : t("login.submit") }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
