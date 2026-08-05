<script setup lang="ts">

import MuSidebar from "@mucom/MuSidebar.vue"
import {onBeforeMount} from "vue"
import {AppInfo} from "@/main.ts"

onBeforeMount(async () => {
  document.title = AppInfo.appName;
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
