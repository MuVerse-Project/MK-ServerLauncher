import {computed, createApp} from 'vue'
import {createPinia} from 'pinia'
import App from '@/App.vue'
import {router} from '@/router'
import {createI18n, useI18n} from "vue-i18n"
import {Box, Home} from "@lucide/vue";
import {useStorage} from "@vueuse/core";
import {useEnvImporterKey} from "@view/muenv/muenv.ts";

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

export const i18n = createI18n({
  legacy: false,
  locale: '简体中文',
  fallbackLocale: 'English',
  allowComposition: true,
  messages: {
    '简体中文':{
      login: {
        title: "需要访问令牌",
        description: "请输入服务端配置的 MuViewAccessToken。验证通过后会保持登录 12 小时。",
        placeholder: "访问令牌",
        submit: "进入",
        submitting: "验证中...",
        failed: "令牌不正确,请重试",
        changed: "令牌已更新,请用新令牌重新登录",
        bootstrapTitle: "首次部署 · 一次性临时令牌",
        bootstrapHint: "先用这枚临时令牌登录,随后会被要求设置你自己的令牌。它只在内存里,重启即失效。",
        bootstrapCopy: "复制",
        bootstrapCopied: "已复制",
        toggleTheme: "切换深浅色",
        switchLocale: "切换语言",
      },
      setup: {
        title: "设置你自己的访问令牌",
        description: "临时令牌只在内存里、重启即失效。设置后旧会话全部失效,需要用新令牌重新登录。",
        token: "新令牌(至少 16 位)",
        confirm: "再输一次",
        submit: "保存并重新登录",
        submitting: "保存中...",
        tooShort: "令牌至少 {min} 位",
        mismatch: "两次输入不一致",
        failed: "保存失败,请重试",
      },
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
              2: "长度需要在4~20个字符内",
            },
            ev_loc: {
              1: "不能为空",
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
      login: {
        title: "Access token required",
        description: "Enter the MuViewAccessToken configured on the server. The session stays valid for 12 hours.",
        placeholder: "Access token",
        submit: "Enter",
        submitting: "Verifying...",
        failed: "Invalid token, please try again",
        changed: "Token updated, please sign in with the new one",
        bootstrapTitle: "First run · one-time token",
        bootstrapHint: "Sign in with this temporary token first; you will be asked to set your own token. It lives in memory only and is regenerated on restart.",
        bootstrapCopy: "Copy",
        bootstrapCopied: "Copied",
        toggleTheme: "Toggle theme",
        switchLocale: "Switch language",
      },
      setup: {
        title: "Set your own access token",
        description: "The temporary token lives in memory only and is regenerated on restart. After setting a new one, all sessions are invalidated and you need to sign in again.",
        token: "New token (min 16 chars)",
        confirm: "Repeat it",
        submit: "Save and sign in again",
        submitting: "Saving...",
        tooShort: "Token must be at least {min} characters",
        mismatch: "The two inputs do not match",
        failed: "Failed to save, please try again",
      },
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
              1: "Must be a letter at first",
              2: "Must be between 3 and 10 characters",
            },
            ev_loc: {
              1: "Do not empty",
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

export const useLocale = (locale: string) => {
  i18nLocale.value = locale
  // @ts-ignore
  i18n.global.locale.value = i18nLocale.value
  useEnvImporterKey().refreshKey()
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(i18n)
app.use(router)

app.mount('#app')
