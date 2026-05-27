import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Chat',
        component: () => import('../views/Chat.vue'),
        meta: { title: 'AI 对话' }
      },
      {
        path: 'admin',
        name: 'AdminPanel',
        component: () => import('../views/AdminPanel.vue'),
        meta: { title: '管理面板', requireAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.meta.requireAdmin && role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

export default router
