import { createRouter, createWebHistory } from 'vue-router'
import {refreshSession} from "@muapi/session.ts"
import {hasAccessToken} from "@muapi/accessToken.ts"

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
      // 首次部署用临时令牌登录后,必须先把令牌换成自己的
      path: '/setup-token',
      name: 'TokenSetup',
      component: () => import('@muview/TokenSetupView.vue'),
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
  // 令牌只活在当前页面实例里:没有令牌(含刷新、重开标签)一律先回登录页
  if (!hasAccessToken() && !to.meta.public) {
    return {path: '/login'}
  }
  const session = await refreshSession()
  // 服务端未启用鉴权:登录页没有意义,直接回首页
  if (!session.authEnabled) {
    return to.meta.public && to.name === 'Login' ? {path: '/'} : true
  }
  // 首次初始化(用户还没设置自己的令牌):登录后强制先去设置页
  if (session.bootstrap) {
    if (!session.authenticated) {
      return to.name === 'Login' ? true : {path: '/login'}
    }
    return to.name === 'TokenSetup' ? true : {path: '/setup-token'}
  }
  if (to.meta.public) {
    return session.authenticated ? {path: '/'} : true
  }
  return session.authenticated ? true : {path: '/login'}
})
