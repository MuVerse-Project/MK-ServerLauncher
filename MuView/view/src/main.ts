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
      muenv:{
        title: "环境",
        filterPlaceholder: "通过环境名查找...",
        importer: {
          button: "导入",
          title: "导入环境",
          name: "环境名",
          path: "路径",
          cancel: "取消",
          import: "导入",
          validator: {
            ev_name: {
              1: "必须要以字母开头",
              2: "长度不可超过20个字符",
            }
          }
        },
        deleter: {
          title: "确定？",
          description: "确定要删除这个环境？(这不会删除文件)",
          description2: "如果你想删除之后重新导入这个环境，你可以通过“导入”按键重新导入",
          cancel: "取消",
          confirm: "确认",
        },
        table: {
          title: {
            name: "环境名",
            version: "版本（提供商版本）",
          },
          page: {
            previous: "上一页",
            next: "下一页",
          },
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
      muenv:{
        title: "Environment",
        filterPlaceholder: "Filter Name...",
        importer: {
          button: "Import",
          title: "Import Environment",
          name: "Name",
          path: "Path",
          cancel: "Cancel",
          import: "Import",
          validator: {
            ev_name: {
              1: "Must be an alphabet at first",
              2: "The length do not more than 20",
            }
          }
        },
        deleter: {
          title: "Confirm",
          description: "Surely to Delete this Environment? \n (Not delete file)",
          description2: "If you want to re-register this Environment after delete, you can using \"Import\" on Environment Page",
          cancel: "Cancel",
          confirm: "Confirm",
        },
        table: {
          title: {
            name: "Name",
            version: "Version (Vendor)",
          },
          page: {
            previous: "Previous",
            next: "Next",
          },
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
