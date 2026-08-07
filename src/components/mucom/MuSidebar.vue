<script setup lang="ts">
import {
  Sidebar,
  SidebarContent, SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader, SidebarInset,
  SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarProvider, SidebarRail, SidebarTrigger
} from "@shadcn/sidebar"
import {GalleryVerticalEnd, SquareUserRound, Moon, Sun, Languages} from "@lucide/vue"
import {AppInfo, useLocale, useSidebarMenus} from "@/main.ts"
import {OverlayScrollbarsComponent} from "overlayscrollbars-vue"
import {useColorMode} from "@vueuse/core";
import {TooltipProvider, Tooltip, TooltipContent, TooltipTrigger} from "@shadcn/tooltip";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@shadcn/dropdown-menu";

const mode = useColorMode()
const { menus } = useSidebarMenus()
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
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem v-for="item in menus" :key="item.title">
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger as-child>
                        <SidebarMenuButton as-child>
                          <RouterLink :to="item.url">
                            <component :is="item.icon" />
                            <span>{{ item.title }}</span>
                          </RouterLink>
                        </SidebarMenuButton>
                      </TooltipTrigger>
                      <TooltipContent
                          align="center" side="right"
                          :align-offset="5"
                          :avoid-collisions="true"
                          :collision-boundary="null"
                          :collision-padding="2"
                          :arrow-padding="2"
                          :hide-when-detached="true"
                          position-strategy="absolute"
                          update-position-strategy="always"
                          sticky="always"
                      >
                        {{ item.title }}
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </OverlayScrollbarsComponent>
        <SidebarGroup class="mt-auto">
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <SidebarMenuButton>
                      <Languages/>
                      <span>{{ $t("sidebar.internal.localeSelector") }}</span>
                    </SidebarMenuButton>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent>
                    <DropdownMenuItem v-for="l in $i18n.availableLocales" @click="useLocale(l)">{{ l }}</DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </SidebarMenuItem>
              <SidebarMenuItem>
                <SidebarMenuButton v-if="mode == 'light'" @click="mode = 'dark'">
                  <Moon/>
                  <span>{{ $t("sidebar.internal.colorChanger.inLight") }}</span>
                </SidebarMenuButton>
                <SidebarMenuButton v-else @click="mode = 'light'">
                  <Sun/>
                  <span>{{ $t("sidebar.internal.colorChanger.inDark") }}</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter> <!-- MuUser Component -->
        <SidebarMenuButton size="lg">
          <div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
            <SquareUserRound class="size-4" />
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
      <div class="flex flex-wrap max-h-dvh gap-5 px-5">
        <slot/>
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>

<style scoped>

</style>