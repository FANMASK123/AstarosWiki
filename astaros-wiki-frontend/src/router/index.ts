import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue'),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
    },
    {
      path: '/category/:id',
      name: 'Category',
      component: () => import('../views/Category.vue'),
    },
    {
      path: '/tag/:name',
      name: 'Tag',
      component: () => import('../views/Tag.vue'),
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('../views/Search.vue'),
    },
    {
      path: '/page/:slug',
      name: 'PageDetail',
      component: () => import('../views/page/PageDetail.vue'),
    },
    {
      path: '/edit/:slug',
      name: 'PageEdit',
      component: () => import('../views/page/PageEdit.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/page/:slug/edit',
      name: 'PageEditAlias',
      component: () => import('../views/page/PageEdit.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/page/:slug/history',
      name: 'PageHistory',
      component: () => import('../views/page/PageHistory.vue'),
    },
    {
      path: '/create',
      name: 'PageCreate',
      component: () => import('../views/page/PageEdit.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/pages',
      name: 'PageCenter',
      component: () => import('../views/page/PageCenter.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'create',
          name: 'PageCenterCreate',
          component: () => import('../views/page/PageEdit.vue'),
        },
        {
          path: 'edit',
          name: 'PageCenterEdit',
          component: () => import('../views/page/PageEditList.vue'),
        },
        {
          path: 'edit/:slug',
          name: 'PageCenterEditSlug',
          component: () => import('../views/page/PageEdit.vue'),
        },
        {
          path: 'manage',
          name: 'PageCenterManage',
          component: () => import('../views/page/PagePublish.vue'),
        },
      ],
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('../views/admin/Dashboard.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('../views/admin/Users.vue'),
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('../views/admin/Categories.vue'),
        },
        {
          path: 'tags',
          name: 'AdminTags',
          component: () => import('../views/admin/Tags.vue'),
        },
        {
          path: 'pages',
          name: 'AdminPages',
          component: () => import('../views/admin/Pages.vue'),
        },
        {
          path: 'pages/review/:slug',
          name: 'AdminPageReview',
          component: () => import('../views/admin/PageReview.vue'),
        },
        {
          path: 'logs',
          name: 'AdminLogs',
          component: () => import('../views/admin/Logs.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn()) {
      next('/login')
      return
    }

    if (to.meta.requiresAdmin && !userStore.isAdmin()) {
      next('/')
      return
    }
  }

  next()
})

export default router
