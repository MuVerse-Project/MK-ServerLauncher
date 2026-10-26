<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {useI18n} from "vue-i18n"
import {useColorMode} from "@vueuse/core"
import {Languages, Moon, Sun} from "@lucide/vue"
import {Button} from "@shadcn/button"
import {Input} from "@shadcn/input"
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@shadcn/card"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@shadcn/dropdown-menu"
import {login} from "@muapi/session.ts"
import {AppInfo, useLocale} from "@/main.ts"

/**
 * ## MuView 登录页
 *
 * 与主界面共用同一套 shadcn 组件、主题变量与 i18n,因此不存在风格割裂。
 * 右上角提供语言与深浅色切换(登录前也能用,免得进不去就没法换语言/主题)。
 *
 * @since VoidLand V0 | DEV.1
 */
const {t} = useI18n()
const router = useRouter()
const mode = useColorMode()

const token = ref("")
const submitting = ref(false)
const failed = ref(false)

const themeIcon = computed(() => (mode.value === "dark" ? Sun : Moon))

function toggleTheme() {
  mode.value = mode.value === "dark" ? "light" : "dark"
}

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
  <div class="relative flex min-h-screen items-center justify-center bg-background p-6">
    <!-- 登录前也能切语言与主题 -->
    <div class="absolute right-4 top-4 flex items-center gap-1">
      <Button variant="ghost" size="icon" :title="t('login.toggleTheme')" @click="toggleTheme">
        <component :is="themeIcon"/>
      </Button>
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <Button variant="ghost" size="icon" :title="t('login.switchLocale')">
            <Languages/>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem v-for="l in $i18n.availableLocales" :key="l" @click="useLocale(l)">
            {{ l }}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <Card class="w-full max-w-sm">
      <CardHeader>
        <div class="mb-1 flex items-baseline gap-2">
          <span class="text-sm font-medium tracking-wide">{{ AppInfo.appName }}</span>
          <span class="text-xs text-muted-foreground">{{ AppInfo.version }}</span>
        </div>
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
