<script setup lang="ts">
/**
 * ## 主布局
 *
 * 侧边栏 + 内容区。只有需要侧边栏的页面挂在这层下面;
 * 登录页这类"独立页面"直接挂在根路由上,不会出现在 MuSidebar 右侧。
 *
 * @since VoidLand V0 | DEV.1
 */
import MuSidebar from "@mucom/MuSidebar.vue"
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
