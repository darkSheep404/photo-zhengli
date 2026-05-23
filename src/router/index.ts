import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomePage.vue'),
      meta: { showTabs: true },
    },
    {
      path: '/cleanup',
      name: 'cleanup-config',
      component: () => import('@/views/CleanupConfigPage.vue'),
      meta: { showTabs: true },
    },
    {
      path: '/cleanup/session',
      name: 'cleanup-session',
      component: () => import('@/views/CleanupPage.vue'),
      meta: { showTabs: false },
    },
    {
      path: '/review',
      name: 'review',
      component: () => import('@/views/ReviewPage.vue'),
      meta: { showTabs: false },
    },
    {
      path: '/albums',
      name: 'albums',
      component: () => import('@/views/AlbumsPage.vue'),
      meta: { showTabs: true },
    },
    {
      path: '/stats',
      name: 'stats',
      component: () => import('@/views/StatsPage.vue'),
      meta: { showTabs: true },
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsPage.vue'),
      meta: { showTabs: true },
    },
  ],
})

export default router
