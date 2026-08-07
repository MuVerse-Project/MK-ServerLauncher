<script setup lang="ts">
import {AppInfo} from "@/main.ts";
import {X} from "@lucide/vue"
import {Card, CardContent} from "@shadcn/card";
import {Separator} from "@shadcn/separator";

</script>

<template>
  <Card>
    <CardContent>
      <div class="flex flex-row not-md:flex-col items-center justify-center gap-5">
        <div>
          <div class="text-5xl font-bold underline">{{ AppInfo.appName }}</div>
          <div class="-mt-0.5 text-xl">Ver.{{ AppInfo.version }}</div>
          <div class="text-sm text-muted-foreground">By {{ AppInfo.dev.devName }}</div>
        </div>
        <X class="not-md:hidden" :size="32" />
        <div>
          <div class="text-5xl font-bold underline">MuView</div>
          <div class="-mt-0.5 text-xl">Ver.LunaLight V0.1.5</div>
          <div class="text-sm text-muted-foreground">By Mu_Mu</div>
        </div>
      </div>
      <separator class="my-4 border-2 border-black dark:border-gray-400" />
      <div>

      </div>
    </CardContent>
  </Card>
</template>

<style scoped>

</style>