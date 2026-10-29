<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import {useI18n} from "vue-i18n"
import {useColorMode} from "@vueuse/core"
import {Languages, Moon, Sun} from "@lucide/vue"
import {Button} from "@shadcn/button"
import {Input} from "@shadcn/input"
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@shadcn/card"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@shadcn/dropdown-menu"
import {fetchBootstrapToken} from "@muapi/auth.ts"
import {login, muSession, refreshSession} from "@muapi/session.ts"
import {AppInfo, useLocale} from "@/main.ts"

/**
 * ## MuView 登录页
 *
 * 与主界面共用同一套 shadcn 组件、主题变量与 i18n,因此不存在风格割裂。
 * 右上角提供语言与深浅色切换(登录前也能用,免得进不去就没法换语言/主题)。
 *
 * 首次部署时(配置里还没有令牌)服务端会生成一次性临时令牌,这里直接显示出来,
 * 让用户能用它登录、随后在设置页换成自己的令牌。
 *
 * @since VoidLand V0 | DEV.1
 */
const {t} = useI18n()
const router = useRouter()
const route = useRoute()
const mode = useColorMode()

const token = ref("")
const submitting = ref(false)
const failed = ref(false)

/** 首次部署的一次性临时令牌(只在 bootstrap 阶段有值)。 */
const bootstrapToken = ref("")
const copied = ref(false)

const justChanged = computed(() => route.query.changed === "1")
const themeIcon = computed(() => (mode.value === "dark" ? Sun : Moon))

function toggleTheme() {
  mode.value = mode.value === "dark" ? "light" : "dark"
}

async function copyBootstrap() {
  if (!bootstrapToken.value) return
  try {
    await navigator.clipboard.writeText(bootstrapToken.value)
    copied.value = true
    window.setTimeout(() => (copied.value = false), 1500)
  } catch {
    // 剪贴板不可用(非 https / 无权限)时保持原样,让用户手动抄
  }
}

async function submit() {
  if (!token.value.trim() || submitting.value) return
  submitting.value = true
  failed.value = false
  const ok = await login(token.value.trim())
  submitting.value = false
  if (ok) {
    // 首次初始化时守卫会把我们送去 /setup-token
    await router.replace(muSession.bootstrap ? "/setup-token" : "/")
  } else {
    failed.value = true
  }
}

onMounted(async () => {
  await refreshSession()
  if (!muSession.bootstrap) return
  try {
    bootstrapToken.value = (await fetchBootstrapToken())?.token ?? ""
  } catch {
    // 非本机访问或接口不可用:不显示令牌,用户需去配置文件里查
    bootstrapToken.value = ""
  }
})
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
        <!-- 首次部署:把临时令牌直接亮出来,省得用户去翻日志 -->
        <div v-if="bootstrapToken" class="mb-4 rounded-md border border-dashed p-3">
          <p class="mb-1 text-xs font-medium">{{ t("login.bootstrapTitle") }}</p>
          <div class="flex items-center gap-2">
            <code class="flex-1 break-all font-mono text-xs">{{ bootstrapToken }}</code>
            <Button size="sm" variant="secondary" type="button" @click="copyBootstrap">
              {{ copied ? t("login.bootstrapCopied") : t("login.bootstrapCopy") }}
            </Button>
          </div>
          <p class="mt-2 text-xs leading-relaxed text-muted-foreground">{{ t("login.bootstrapHint") }}</p>
        </div>

        <p v-if="justChanged" class="mb-3 text-sm text-emerald-600 dark:text-emerald-400">
          {{ t("login.changed") }}
        </p>

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
