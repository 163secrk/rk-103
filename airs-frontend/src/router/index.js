import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const roleMenus = {
  ADMIN: ['dashboard', 'athletes', 'injury', 'rehabilitation', 'training', 'users'],
  DOCTOR: ['dashboard', 'athletes', 'injury', 'rehabilitation'],
  THERAPIST: ['dashboard', 'athletes', 'rehabilitation'],
  COACH: ['dashboard', 'athletes', 'training'],
  ATHLETE: ['dashboard', 'athletes']
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', menu: 'dashboard' }
      },
      {
        path: 'athletes',
        name: 'Athletes',
        component: () => import('@/views/athletes/index.vue'),
        meta: { title: '运动员管理', menu: 'athletes' }
      },
      {
        path: 'injury',
        name: 'Injury',
        component: () => import('@/views/injury/index.vue'),
        meta: { title: '伤病记录', menu: 'injury' }
      },
      {
        path: 'rehabilitation',
        name: 'Rehabilitation',
        component: () => import('@/views/rehabilitation/index.vue'),
        meta: { title: '康复计划', menu: 'rehabilitation' }
      },
      {
        path: 'training',
        name: 'Training',
        component: () => import('@/views/common/Placeholder.vue'),
        meta: { title: '训练计划', menu: 'training' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/index.vue'),
        meta: { title: '用户管理', menu: 'users' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - AIRS` : 'AIRS - 运动员伤病康复追踪系统'
  const token = getToken()
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      if (to.meta.menu && userStore.userInfo) {
        const allowedMenus = roleMenus[userStore.userInfo.role] || []
        if (!allowedMenus.includes(to.meta.menu)) {
          next('/dashboard')
          return
        }
      }
      next()
    }
  }
})

export default router
