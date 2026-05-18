<template>
  <div class="app">
    <el-menu mode="horizontal" :ellipsis="false" class="nav-menu">
      <el-menu-item index="home" @click="$router.push('/')">主页</el-menu-item>
      <el-menu-item index="products" @click="$router.push('/products')">商品列表</el-menu-item>
      <template v-if="isLoggedIn">
        <el-menu-item index="cart" @click="$router.push('/cart')">购物车</el-menu-item>
        <el-menu-item index="orders" @click="$router.push('/orders')">我的订单</el-menu-item>
        <el-menu-item index="profile" @click="$router.push('/profile')">个人中心</el-menu-item>
        <template v-if="role === 'merchant' || role === 'admin'">
          <el-menu-item index="merchant-products" @click="$router.push('/merchant/products')">商品管理</el-menu-item>
          <el-menu-item index="merchant-orders" @click="$router.push('/merchant/orders')">订单管理</el-menu-item>
        </template>
        <template v-if="role === 'admin'">
          <el-menu-item index="admin-users" @click="$router.push('/admin/audit-users')">用户审核</el-menu-item>
          <el-menu-item index="admin-products" @click="$router.push('/admin/audit-products')">商品审核</el-menu-item>
          <el-menu-item index="admin-all-users" @click="$router.push('/admin/users')">用户管理</el-menu-item>
          <el-menu-item index="admin-level" @click="$router.push('/admin/merchant-level')">商家等级</el-menu-item>
        </template>
      </template>
      <div class="nav-spacer" />
      <template v-if="isLoggedIn">
        <el-menu-item index="logout" @click="logout">退出登录</el-menu-item>
      </template>
      <template v-else>
        <el-menu-item index="login" @click="$router.push('/login')">登录</el-menu-item>
        <el-menu-item index="register" @click="$router.push('/register')">注册</el-menu-item>
      </template>
    </el-menu>

    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const role = computed(() => localStorage.getItem('role') || 'user')

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #f5f7fa;
}

.nav-menu {
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-spacer {
  flex: 1;
}
</style>
