<script setup lang="ts">
import {cn} from "@/lib/utils.ts";
import {computed, type HTMLAttributes, ref} from "vue";

const props = defineProps<{
  value: number,
  max: number,
  class?: HTMLAttributes['class']
}>()
let value = computed(() => props.value)
let max = computed(() => props.max)
let percent = computed(() => Math.min((props.value / props.max) * 100, 100).toFixed(2))
</script>

<template>
  <div class="flex w-full max-w-sm items-center gap-5 rounded-xl bg-white p-4 shadow-lg ring-1 ring-black/5 dark:bg-gray-800">
    <div class="grid grid-cols-1 grid-rows-1">
      <div class="col-start-1 row-start-1 size-12 rounded-full border-4 border-gray-100 dark:border-gray-700"></div>
      <div
          :class="cn(`gauge-ring col-start-1 row-start-1 size-12 rounded-full border-4 border-amber-500 dark:border-amber-400 mask-conic-from-(--progress) mask-conic-to-(--progress)`)"
          :style="{ '--progress': `${percent}%` }"
      ></div>
    </div>
    <div class="w-0 flex-1 text-sm text-gray-950 dark:text-white">
      <p class="font-medium">Storage used: {{ percent }}%</p>
      <p class="mt-1 text-gray-500 dark:text-gray-400">
        <span class="font-medium">{{ value }} GB</span> out of {{ max }} GB remaining
      </p>
    </div>
  </div>
</template>

<style scoped>
@property --progress {
  syntax: '<percentage>';
  inherits: false;
  initial-value: 0%;
}

.gauge-ring {
  transition: --progress 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}
</style>