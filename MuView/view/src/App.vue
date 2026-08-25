<script setup lang="ts">

import MuSidebar from "@mucom/MuSidebar.vue"
import {computed, onBeforeMount} from "vue"
import {AppInfo} from "@/main.ts"

import 'vue-sonner/style.css'
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
  <MuSidebar>
    <RouterView v-slot="{ Component }">
      <transition name="slide-fade" appear>
        <component :is="Component" />
      </transition>
    </RouterView>
  </MuSidebar>
  <Toaster position="top-right" :theme="sonnerTheme"/>
</template>

<style scoped>
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}
</style>
