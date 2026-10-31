<script setup lang="ts">

import {computed, onBeforeMount} from "vue"
import {AppInfo} from "@/main.ts"

import '@/style.css'
import 'vue-sonner/style.css'
import 'overlayscrollbars/overlayscrollbars.css'
import {Toaster} from "vue-sonner";
import {useColorMode} from "@vueuse/core";

onBeforeMount(async () => {
  document.title = AppInfo.appName;
})

const theme = useColorMode()
const sonnerTheme = computed(() => {
  if(theme.value === 'auto'){
    return 'system'
  }
  return theme.value
})

</script>

<template>
  <!-- 侧边栏等外壳由 MainLayout 提供;登录页这类独立页面不经过它 -->
  <RouterView />
  <Toaster position="top-right" :theme="sonnerTheme"/>
</template>
