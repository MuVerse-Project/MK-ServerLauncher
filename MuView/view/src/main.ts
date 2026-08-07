import {computed, createApp} from 'vue'
import {createPinia, defineStore} from 'pinia'
import '@/style.css'
import App from '@/App.vue'
import {router} from '@/router'
import {createI18n, useI18n} from "vue-i18n"
import 'overlayscrollbars/overlayscrollbars.css'
import 'vue-sonner/style.css'
import {Home, NotebookText} from "@lucide/vue";
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
        example: "示例页面",
        home: "主页面",
        internal: {
          localeSelector: "语言",
          colorChanger: {
            inDark: "明亮模式",
            inLight: "暗黑模式"
          },
        }
      },
    },
    'English':{
      sidebar:{
        example: "Example",
        home: "Home",
        internal: {
          localeSelector: "Language",
          colorChanger: {
            inDark: "Light Mode",
            inLight: "Dark Mode"
          },
        }
      },
    },
  }
})

// Define MuSidebar Menus
export function useSidebarMenus() {
  const { t } = useI18n()

  const menus = computed(() => [
    {
      title: t('sidebar.example'),
      url: '/example',
      icon: NotebookText,
    },
    {
      title: t('sidebar.home'),
      url: '/',
      icon: Home,
    },
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
