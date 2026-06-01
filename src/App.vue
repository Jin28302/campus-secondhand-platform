<!--
  应用根组件 - 全局导航栏 + 路由视图
  主要功能：
    - 顶部导航菜单：根据登录状态和角色动态显示菜单项
    - 未登录：主页、商品列表、登录、注册
    - 已登录（用户）：主页、商品列表、购物车、我的订单、个人中心、退出登录
    - 已登录（商家/管理员）：额外显示商品管理、订单管理
    - 已登录（管理员）：额外显示用户审核、商品审核、用户管理、商家等级
    - router-view：根据当前路由渲染对应的页面组件
-->
<template>
  <div class="app">
    <!-- 顶部导航栏 -->
    <el-menu mode="horizontal" :ellipsis="false" class="nav-menu">
      <!-- 公共菜单项：主页、商品列表 -->
      <el-menu-item index="home" @click="$router.push('/')">主页</el-menu-item>
      <el-menu-item index="products" @click="$router.push('/products')">商品列表</el-menu-item>

      <!-- 登录后的菜单项 -->
      <template v-if="isLoggedIn">
        <el-menu-item index="cart" @click="$router.push('/cart')">购物车</el-menu-item>
        <el-menu-item index="orders" @click="$router.push('/orders')">我的订单</el-menu-item>
        <el-menu-item index="profile" @click="$router.push('/profile')">个人中心</el-menu-item>

        <!-- 商家/管理员专属菜单 -->
        <template v-if="role === '商家' || role === '管理员'">
          <el-menu-item index="merchant-products" @click="$router.push('/merchant/products')">商品管理</el-menu-item>
          <el-menu-item index="merchant-orders" @click="$router.push('/merchant/orders')">订单管理</el-menu-item>
        </template>

        <!-- 管理员专属菜单 -->
        <template v-if="role === '管理员'">
          <el-menu-item index="admin-users" @click="$router.push('/admin/audit-users')">用户审核</el-menu-item>
          <el-menu-item index="admin-products" @click="$router.push('/admin/audit-products')">商品审核</el-menu-item>
          <el-menu-item index="admin-all-users" @click="$router.push('/admin/users')">用户管理</el-menu-item>
          <el-menu-item index="admin-level" @click="$router.push('/admin/merchant-level')">商家等级</el-menu-item>
        </template>
      </template>

      <!-- 弹性空间，将右侧菜单推到最右 -->
      <div class="nav-spacer" />

      <!-- 右侧：登录/注册 或 退出登录 -->
      <template v-if="isLoggedIn">
        <el-menu-item index="logout" @click="logout">退出登录</el-menu-item>
      </template>
      <template v-else>
        <el-menu-item index="login" @click="$router.push('/login')">登录</el-menu-item>
        <el-menu-item index="register" @click="$router.push('/register')">注册</el-menu-item>
      </template>
    </el-menu>

    <!-- 路由视图：根据当前路径渲染对应页面组件 -->
    <router-view />
  </div>
</template>

<script setup>
/**
 * 应用根组件
 * 从 Pinia user store 读取登录状态和角色，动态控制菜单显示
 * 退出登录时清除状态并跳转到登录页
 */
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'

/** Vue Router 实例 */
const router = useRouter()
/** Pinia user store 实例 */
const userStore = useUserStore()
/** 解构响应式状态：isLoggedIn（登录状态）、role（用户角色） */
const { isLoggedIn, role } = storeToRefs(userStore)

/**
 * 退出登录
 * 调用 userStore.logout() 清除 token/role
 * 跳转到登录页
 */
function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏粘性定位，始终在页面顶部 */
.nav-menu {
  position: sticky;
  top: 0;
  z-index: 100;
}

/* 弹性空间，将右侧菜单推到最右侧 */
.nav-spacer {
  flex: 1;
}
</style>
