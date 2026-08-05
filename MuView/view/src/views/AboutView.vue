<script setup lang="ts">
import {AppInfo} from "@/main.ts";
import {X} from "@lucide/vue"
import {Card, CardContent} from "@shadcn/card";

</script>

<template>
  <Card>
    <CardContent>
      <div class="flex flex-wrap items-center gap-5">
        <div>
          <div class="text-5xl font-bold underline">{{ AppInfo.appName }}</div>
          <div class="-mt-0.5 text-xl">Ver.{{ AppInfo.version }}</div>
          <div class="text-sm text-muted-foreground">By {{ AppInfo.dev.devName }}</div>
        </div>
        <X :size="32" />
        <div>
          <div class="text-5xl font-bold underline">MuView</div>
          <div class="-mt-0.5 text-xl">Ver.LunaLight V0</div>
          <div class="text-sm text-muted-foreground">By Mu_Mu</div>
        </div>
      </div>
      <div>Not OpenSource.</div>
    </CardContent>
  </Card>
</template>

<style scoped>

</style>