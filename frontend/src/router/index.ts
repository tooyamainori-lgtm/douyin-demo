import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/HomeView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/login/RegisterView.vue'),
    },
    {
      path: '/video/:id',
      name: 'video-detail',
      component: () => import('@/views/video/VideoDetailView.vue'),
    },
    {
      path: '/user/:id',
      name: 'user-profile',
      component: () => import('@/views/user/UserProfileView.vue'),
    },
    {
      path: '/upload',
      name: 'upload',
      component: () => import('@/views/upload/UploadView.vue'),
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/favorites/FavoritesView.vue'),
    },
  ],
})

export default router
