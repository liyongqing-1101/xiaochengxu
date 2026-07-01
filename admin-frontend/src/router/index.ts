import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AdminLayout from '@components/layout/AdminLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/question',
    children: [
      {
        path: 'question',
        name: 'Question',
        component: () => import('@views/question/index.vue'),
        meta: { title: '题库管理' },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@views/category/index.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'import',
        name: 'Import',
        component: () => import('@views/import/index.vue'),
        meta: { title: '导入记录' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  // Session模式：检查用户信息是否存在（admin_nickname）
  const nickname = localStorage.getItem('admin_nickname')
  if (to.path !== '/login' && !nickname) {
    next('/login')
  } else if (to.path === '/login' && nickname) {
    // 已登录用户访问登录页，跳转到首页
    next('/question')
  } else {
    next()
  }
})

export default router
