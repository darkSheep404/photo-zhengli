import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomePage.vue'),
    },
    {
      path: '/cleanup',
      name: 'cleanup',
      component: () => import('@/views/CleanupPage.vue'),
    },
    {
      path: '/review',
      name: 'review',
      component: () => import('@/views/ReviewPage.vue'),
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsPage.vue'),
    },
  ],
})

export default router
