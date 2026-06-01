/**
 * 路由配置 - 页面路由定义 + 全局导航守卫
 *
 * 路由架构：
 * - 公共页面：/login, /register, /, /products, /product/:id（无需登录）
 * - 用户页面：/cart, /checkout, /orders, /profile（需登录）
 * - 商家页面：/merchant, /merchant/products, /merchant/orders（角色：商家或管理员）
 * - 管理员页面：/admin, /admin/audit-users, /admin/audit-products, /admin/users, /admin/merchant-level（角色：管理员）
 *
 * meta 属性说明：
 * - requiresAuth: true → 需要登录，未登录跳转 /login（带 redirect 参数供登录后回跳）
 * - guest: true → 仅未登录可访问（登录/注册页），已登录用户访问会跳转首页
 * - roles: [] → 角色白名单，当前用户角色不在列表中则跳转 /products
 *
 * 全局导航守卫执行顺序：
 * 1. 检查 requiresAuth：未登录 → /login?redirect=
 * 2. 检查 guest：已登录 → /
 * 3. 检查 roles：角色不匹配 → /products
 */
import { createRouter, createWebHistory } from 'vue-router'

/** 路由配置数组 */
const routes = [
  // ======== 公共页面（无需登录即可访问） ========
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true },  // 仅未登录用户可访问，已登录用户访问会跳转首页
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),  // 首页，所有人可访问
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/Products.vue'),  // 商品列表页，所有人可访问
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),  // 商品详情页，所有人可访问
  },

  // ======== 用户页面（需登录） ========
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { requiresAuth: true },  // 需要登录
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true },
  },

  // ======== 商家页面（需登录，角色为"商家"或"管理员"） ========
  // 注意：roles 中的值来自 localStorage 中的 role 字段，与后端角色对应："用户"/"商家"/"管理员"
  {
    path: '/merchant',
    name: 'Merchant',
    component: () => import('@/views/Merchant.vue'),
    meta: { requiresAuth: true, roles: ['商家', '管理员'] },
  },
  {
    path: '/merchant/products',
    name: 'MerchantProducts',
    component: () => import('@/views/MerchantProducts.vue'),
    meta: { requiresAuth: true, roles: ['商家', '管理员'] },
  },
  {
    path: '/merchant/orders',
    name: 'MerchantOrders',
    component: () => import('@/views/MerchantOrders.vue'),
    meta: { requiresAuth: true, roles: ['商家', '管理员'] },
  },

  // ======== 管理员页面（需登录，角色仅"管理员"） ========
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, roles: ['管理员'] },
  },
  {
    path: '/admin/audit-users',
    name: 'AdminAuditUsers',
    component: () => import('@/views/AdminAuditUsers.vue'),
    meta: { requiresAuth: true, roles: ['管理员'] },
  },
  {
    path: '/admin/audit-products',
    name: 'AdminAuditProducts',
    component: () => import('@/views/AdminAuditProducts.vue'),
    meta: { requiresAuth: true, roles: ['管理员'] },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/AdminUsers.vue'),
    meta: { requiresAuth: true, roles: ['管理员'] },
  },
  {
    path: '/admin/merchant-level',
    name: 'AdminMerchantLevel',
    component: () => import('@/views/AdminMerchantLevel.vue'),
    meta: { requiresAuth: true, roles: ['管理员'] },
  },
]

/** 创建 Vue Router 实例，使用 HTML5 History 模式 */
const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局导航守卫
 * 每次路由切换前执行，检查登录状态和角色权限
 *
 * @param {Object} to - 目标路由对象
 * @param {Object} from - 来源路由对象
 * @param {Function} next - 导航确认回调
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role') || 'user'

  // 需要登录的页面：未登录则跳转登录页，并通过 redirect 参数记录目标地址
  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 访客页面（登录/注册）：已登录则跳转首页
  if (to.meta.guest && token) {
    return next('/')
  }

  // 角色权限检查：当前用户角色不在路由允许的角色列表中则跳转商品列表页
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return next('/products')
  }

  next()
})

export default router
