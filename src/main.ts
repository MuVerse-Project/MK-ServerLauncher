import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/style.css'
import App from '@/App.vue'
import {router} from '@/router'
import {createI18n} from "vue-i18n"
import 'overlayscrollbars/overlayscrollbars.css'
import 'vue-sonner/style.css'
import {Home} from "@lucide/vue";

// Define App Info to MuView.
export const AppInfo = {
  appName: "MuView",
  version: "LunaLight V0",
  vercode: 0,
  docLink: undefined,
  repoLink: undefined,
  dev: {
    devName: "Mu_Cloud",
    email: "Mu_Cloud@outlook.de"
  }
}

const i18n= createI18n({
  locale: 'zh',
  fallbackLocale: 'zh',
  allowComposition: true,
  messages: {
    zh:{
      message:{
        title: '',
      }
    }
  }
})

// Define MuSidebar Menus
export const MuSidebarMenus = [
  {
    title: "Home",
    url: "/",
    icon: Home,
  },
]

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(i18n)
app.use(router)

app.mount('#app')
