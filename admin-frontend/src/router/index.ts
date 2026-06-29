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
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
