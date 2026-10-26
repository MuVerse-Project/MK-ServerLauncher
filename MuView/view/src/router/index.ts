import { createRouter, createWebHistory } from 'vue-router'
import {refreshSession} from "@muapi/session.ts"

/**
 * 路由表。
 *
 * `/login` 标记 `meta.public`,守卫放行;其余路由在服务端启用鉴权时,
 * 未登录一律导回 `/login`。
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
