import { createRouter, createWebHistory } from 'vue-router'
import { canAccessRole, fetchSession, type UserRole } from '@muapi/auth'
import {Home, Info} from "@lucide/vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('@/views/AboutView.vue'),
    },
  ],
})
