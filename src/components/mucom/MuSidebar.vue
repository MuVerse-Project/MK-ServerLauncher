<script setup lang="ts">
import {
  Sidebar,
  SidebarContent, SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel, SidebarHeader, SidebarInset,
  SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarProvider, SidebarRail, SidebarTrigger
} from "@shadcn/sidebar"
import {GalleryVerticalEnd, SquareUserRoundIcon} from "@lucide/vue"
import {AppInfo} from "@/main.ts"
import {MuSidebarMenus} from "@router/index.ts"
import {OverlayScrollbarsComponent} from "overlayscrollbars-vue"
</script>

<template>
  <SidebarProvider>
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarMenuButton size="lg">
          <RouterLink to="/about" class="flex flex-row gap-2">
            <div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
              <GalleryVerticalEnd class="size-4" />
            </div>
            <div class="grid flex-1 text-left text-sm leading-tight">
              <span class="truncate font-semibold">{{ AppInfo.appName }}</span>
              <span class="truncate text-xs">{{ AppInfo.version }}</span>
            </div>
          </RouterLink>
        </SidebarMenuButton>
      </SidebarHeader>
      <SidebarContent>
        <OverlayScrollbarsComponent defer
                                    :options="{ scrollbars: { autoHide: 'scroll' } }"
        >
          <SidebarGroup>
            <SidebarGroupLabel>Application</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem v-for="item in MuSidebarMenus" :key="item.title">
                  <SidebarMenuButton as-child>
                    <RouterLink :to="item.url">
                      <component :is="item.icon" />
                      <span>{{ item.title }}</span>
                    </RouterLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </OverlayScrollbarsComponent>
      </SidebarContent>
      <SidebarFooter> <!-- MuUser Component -->
        <SidebarMenuButton size="lg">
          <div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
            <SquareUserRoundIcon class="size-4" />
          </div>
          <div class="grid flex-1 text-left text-sm leading-tight">
            <span class="truncate font-semibold">{{ AppInfo.dev.devName }}</span>
            <span class="truncate text-xs">{{ AppInfo.dev.email }}</span>
          </div>
        </SidebarMenuButton>
      </SidebarFooter>
      <SidebarRail/>
    </Sidebar>
    <SidebarInset>
      <header class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
        <div class="flex items-center gap-2 px-4">
          <SidebarTrigger class="-ml-1" />
        </div>
      </header>
      <div class="px-5">
        <slot/>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>

<style scoped>

</style>