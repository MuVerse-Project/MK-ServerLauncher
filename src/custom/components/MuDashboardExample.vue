<script setup lang="ts">

import MuDashboard from "@mucom/MuDashboard.vue";
</script>

<template>
  <mu-dashboard :value="1526" :max="2666" as="card">
    <template #header="{percent}">
      Wow! It used {{ percent }}%
    </template>
    <template #footer="{value, max}">
      {{ value }} of {{ max }} Used
    </template>
  </mu-dashboard>
</template>