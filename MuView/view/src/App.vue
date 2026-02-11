<script setup lang="ts">
import {ref} from "vue";

let isCollapse = ref(true);

const linkToDoc = () => {
  open("https://mucloudofficial.github.io/MuCloudProjectDoc","_blank", undefined)
}

</script>

<template>
<el-container class="h-lvh">
  <el-header class="main-nav" height="64px">
    <el-row class="h-full" align="middle">
      <img class="mr-5 h-16 w-16" src="./assets/icon.png" alt="LOGO" @click.prevent="isCollapse = !isCollapse">
      <p class="text-2xl mx-5">MK-ServerLauncher<sup>MPE</sup></p>
    </el-row>
  </el-header>
  <el-container class="main-view">
    <el-menu
        :default-active="'/'"
        class="left-menu"
        :collapse="isCollapse"
        router
    >
      <el-menu-item index="/">
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
          </svg>
        </el-icon>
        <template #title><span class="mx-4">Overview</span></template>
      </el-menu-item>
      <el-menu-item index="/servermanager">
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <rect height="8" rx="2" ry="2" width="20" x="2" y="2"/>
            <rect height="8" rx="2" ry="2" width="20" x="2" y="14"/>
            <line x1="6" x2="6.01" y1="6" y2="6"/>
            <line x1="6" x2="6.01" y1="18" y2="18"/>
          </svg>
        </el-icon>
        <template #title>
          <span class="mx-4">Server</span>
        </template>
      </el-menu-item>
      <el-menu-item index="/envmanager">
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" x2="12" y1="22.08" y2="12"/>
          </svg>
        </el-icon>
        <template #title>
          <span class="mx-4">Environment</span>
        </template>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </el-icon>
        <template #title><span class="mx-4">Setting</span></template>
      </el-menu-item>
      <el-divider/>
      <el-menu-item @click.capture="linkToDoc()"><!-- index="5" | Non set index to prevent Change Color when Clicked -->
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </el-icon>
        <template #title>
          <span class="mx-4">
            WIKI
            <el-icon size="15">
              <svg class="stroke-2 stroke-gray-500" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
                <polyline points="15 3 21 3 21 9"/>
                <line x1="10" x2="21" y1="14" y2="3"/>
              </svg>
            </el-icon>
          </span>
        </template>
      </el-menu-item>
      <el-menu-item index="/about">
        <el-icon size="32">
          <svg class="stroke-2 stroke-black" fill="none" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" x2="12" y1="16" y2="12"/>
            <line x1="12" x2="12.01" y1="8" y2="8"/>
          </svg>
        </el-icon>
        <template #title><span class="mx-4">About</span></template>
      </el-menu-item>
    </el-menu>
    <el-main class="!p-0">
      <el-scrollbar >
        <router-view class="p-5" v-slot="{ Component }">
          <transition name="slide-fade">
            <component :is="Component"/>
          </transition>
        </router-view>
      </el-scrollbar>
    </el-main>
  </el-container>
</el-container>
</template>

<style scoped>
.el-menu--collapse{
  box-shadow: var(--el-box-shadow);
  border: 0;
  flex-shrink: 0 !important;
}
.left-menu:not(.el-menu--collapse){
  width: 200px;
  box-shadow: var(--el-box-shadow);
  border: 0;
  flex-shrink: 0 !important;
}
.main-nav{
  padding: 0;
  box-shadow: var(--el-box-shadow);
}
.main-view{
  height: calc(100dvh - 60px);
}
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}
</style>
