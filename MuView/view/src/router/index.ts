import { createRouter, createWebHistory } from 'vue-router'
import {refreshSession} from "@muapi/session.ts"

/**
 * 路由表。
 *
 * 需要侧边栏的页面挂在 `MainLayout` 下;登录页是独立页面,直接挂在根路由上,
 * 因此不会出现在 MuSidebar 右侧。
 *
 * @since VoidLand V0 | DEV.1
 */
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@muview/LoginView.vue'),
      meta: {public: true},
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@muview/HomeView.vue'),
        },
        {
          path: 'about',
          name: 'About',
          component: () => import('@muview/AboutView.vue'),
        },
        {
          path: 'env',
          name: 'Environment',
          component: () => import('@view/muenv/EnvView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const session = await refreshSession()
  // 服务端未启用鉴权:登录页没有意义,直接回首页
  if (!session.authEnabled) {
    return to.meta.public && to.name === 'Login' ? {path: '/'} : true
  }
  if (to.meta.public) {
    return session.authenticated ? {path: '/'} : true
  }
  return session.authenticated ? true : {path: '/login'}
})
