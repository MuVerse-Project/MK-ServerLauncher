import { createRouter, createWebHistory } from 'vue-router'
import { canAccessRole, fetchSession, type UserRole } from '@muapi/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@muview/HomeView.vue'),
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('@muview/AboutView.vue'),
    },
    {
      path: '/example',
      name: 'Example',
      component: () => import('@view/ExampleView.vue'),
    }
  ],
})
