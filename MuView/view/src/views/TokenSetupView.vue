<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {useI18n} from "vue-i18n"
import {Button} from "@shadcn/button"
import {Input} from "@shadcn/input"
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@shadcn/card"
import {updateAccessToken} from "@muapi/auth.ts"
import {setAccessToken} from "@muapi/accessToken.ts"
import {refreshSession} from "@muapi/session.ts"
import {AppInfo} from "@/main.ts"

/**
 * ## 首次部署:设置自己的访问令牌
 *
 * 首次启动时服务端会生成一次性临时令牌(只在登录页展示)。
 * 用临时令牌登录后必须先到这里换成自己的令牌,换完旧会话全部失效,需要重新登录。
 *
 * @since VoidLand V0 | DEV.1
 */
const {t} = useI18n()
const router = useRouter()

const token = ref("")
const confirmToken = ref("")
const submitting = ref(false)
const error = ref("")

const MIN_LENGTH = 16

const mismatch = computed(() => confirmToken.value.length > 0 && token.value !== confirmToken.value)
const tooShort = computed(() => token.value.length > 0 && token.value.trim().length < MIN_LENGTH)
const canSubmit = computed(
    () => token.value.trim().length >= MIN_LENGTH && token.value === confirmToken.value && !submitting.value,
)

function errorMessage(err: unknown): string {
    const body = (err as { response?: { data?: { message?: string } } })?.response?.data
    return body?.message?.trim() || t("setup.failed")
}

async function submit() {
    if (!canSubmit.value) return
    submitting.value = true
    error.value = ""
    const token0 = token.value.trim()
    try {
        await updateAccessToken(token0)
    } catch (err) {
        submitting.value = false
        error.value = errorMessage(err)
        return
    }
    // 令牌已换 → 直接把新令牌放进内存,不用再登一次(旧会话本来就随签名密钥一起失效)
    setAccessToken(token0)
    await refreshSession(true)
    submitting.value = false
    await router.replace({path: "/"})
}
</script>

<template>
  <div class="relative flex min-h-screen items-center justify-center bg-background p-6">
    <Card class="w-full max-w-sm">
      <CardHeader>
        <div class="mb-1 flex items-baseline gap-2">
          <span class="text-sm font-medium tracking-wide">{{ AppInfo.appName }}</span>
          <span class="text-xs text-muted-foreground">{{ AppInfo.version }}</span>
        </div>
        <CardTitle class="text-xl">{{ t("setup.title") }}</CardTitle>
        <CardDescription class="leading-relaxed">{{ t("setup.description") }}</CardDescription>
      </CardHeader>
      <CardContent>
        <form class="flex flex-col gap-4" @submit.prevent="submit">
          <Input
              v-model="token"
              type="password"
              autocomplete="new-password"
              :placeholder="t('setup.token')"
              :aria-invalid="tooShort"
              autofocus
          />
          <Input
              v-model="confirmToken"
              type="password"
              autocomplete="new-password"
              :placeholder="t('setup.confirm')"
              :aria-invalid="mismatch"
          />
          <p v-if="tooShort" class="text-sm text-destructive">{{ t("setup.tooShort", {min: MIN_LENGTH}) }}</p>
          <p v-else-if="mismatch" class="text-sm text-destructive">{{ t("setup.mismatch") }}</p>
          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>
          <Button type="submit" class="w-full" :disabled="!canSubmit">
            {{ submitting ? t("setup.submitting") : t("setup.submit") }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
