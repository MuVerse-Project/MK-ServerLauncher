import {computed, createApp} from 'vue'
import {createPinia} from 'pinia'
import '@/style.css'
import App from '@/App.vue'
import {router} from '@/router'
import {createI18n, useI18n} from "vue-i18n"
import 'overlayscrollbars/overlayscrollbars.css'
import {Box, Home} from "@lucide/vue";
import {useStorage} from "@vueuse/core";

// Define App Info to MuView.
export const AppInfo = {
  appName: "MK-ServerLauncher",
  version: "VoidLand V0",
  vercode: 0,
  docLink: "https://doc.muverse.ltd/mksl-main.html",
  repoLink: "https://www.github.com/MuVerse-Project/MK-ServerLauncher",
  dev: {
    devName: "Mu_Cloud",
    email: "Mu_Cloud@outlook.de"
  }
}

const i18n= createI18n({
  legacy: false,
  locale: '简体中文',
  fallbackLocale: 'English',
  allowComposition: true,
  messages: {
    '简体中文':{
      sidebar:{
        internal: {
          localeSelector: "语言",
          colorChanger: {
            inDark: "明亮模式",
            inLight: "暗黑模式"
          },
        },
        home: "主页面",
        env: "环境管理",
      },
      muPerformanceCard: {
        title: "总览",
        server: {
          title: "服务器",
          online: "正在运行的",
          stopped: "已停止的",
          total: "总量",
        },
        performance: {
          title: "性能",
          cpu: "CPU 用量",
          mem: "运行内存用量",
        },
        appInfo: {
          title: "MK-ServerLauncher 应用信息",
          coreLabel: "核心",
          versionLabel: "核心版本",
        },
      },
    },
    'English':{
      sidebar:{
        internal: {
          localeSelector: "Language",
          colorChanger: {
            inDark: "Light Mode",
            inLight: "Dark Mode"
          },
        },
        home: "Home",
        env: "Environment"
      },
      muPerformanceCard: {
        title: "Overview",
        server: {
          title: "Server",
          online: "Online",
          stopped: "Stopped",
          total: "Total",
        },
        performance: {
          title: "Performance",
          cpu: "CPU Usage",
          mem: "MEM Usage",
        },
        appInfo: {
          title: "MK-ServerLauncher APP Info",
          coreLabel: "Core",
          versionLabel: "Version",
        },
      },
    },
  }
})

// Define MuSidebar Menus
export function useSidebarMenus() {
  const { t } = useI18n()

  const menus = computed(() => [
    {
      title: t('sidebar.home'),
      url: '/',
      icon: Home,
    },
    {
      title: t('sidebar.env'),
      url: '/env',
      icon: Box,
    }
  ])

  return { menus }
}

export const i18nLocale = useStorage('locale', i18n.global.locale)

export function useLocale(locale: string) {
  i18nLocale.value = locale
  // @ts-ignore
  i18n.global.locale.value = i18nLocale.value
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(i18n)
app.use(router)

app.mount('#app')
