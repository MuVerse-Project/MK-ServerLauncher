import { createRouter, createWebHistory } from 'vue-router'

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
      path: '/env',
      name: 'Environment',
      component: () => import('@view/muenv/EnvView.vue')
    },
  ],
})
