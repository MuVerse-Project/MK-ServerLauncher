<script setup lang="ts">
import {cn} from "@/lib/utils.ts";
import {computed, type HTMLAttributes, ref} from "vue";

const props = defineProps<{
  value: number,
  max: number,
  as: "card" | "plain"
  class?: HTMLAttributes['class']
}>()

let percent = computed(() => Math.min((props.value / props.max) * 100, 100).toFixed(2))

let borderClass = computed(() => {
  if(props.as === "card") {
    return 'flex w-full max-w-sm items-center gap-5 rounded-xl bg-white p-4 shadow-lg ring-1 ring-black/5 dark:bg-sidebar'
  }else if(props.as === "plain") {
    return 'flex w-full max-w-sm items-center gap-5 p-4'
  }
})

</script>

<template>
  <div :class="borderClass">
    <div class="grid grid-cols-1 grid-rows-1">
      <div class="col-start-1 row-start-1 size-12 rounded-full border-4 dark:border-gray-100"></div>
      <div
          :class="cn(`gauge-ring col-start-1 row-start-1 size-12 rounded-full border-4 border-fuchsia-600 dark:border-fuchsia-600 mask-conic-from-(--progress) mask-conic-to-(--progress)`)"
          :style="{ '--progress': `${percent}%` }"
      ></div>
    </div>
    <div class="w-0 flex-1 text-sm text-gray-950 dark:text-white">
      <div class="text-lg font-bold">
        <slot name="header" :percent="percent" :value="value" :max="max">
          <p>{{ percent }}%</p>
        </slot>
      </div>
      <div class="mt-1 text-gray-500 dark:text-gray-400">
        <slot name="footer" :percent="percent" :value="value" :max="max">
          <span class="font-bold">{{ props.value }}</span> / {{ props.max }}
        </slot>
      </div>
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