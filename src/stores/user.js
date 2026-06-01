/**
 * 用户状态管理（Pinia Store） - 管理登录状态和用户信息
 *
 * 职责：
 * - 存储当前用户的 token 和 role（支持页面刷新后从 localStorage 恢复）
 * - 提供 isLoggedIn 计算属性判断登录状态
 * - 提供 login(data) 方法：保存 token/role 到 store 和 localStorage
 * - 提供 logout() 方法：清除 token/role（store + localStorage）
 *
 * 数据持久化：token 和 role 同时存储在 Pinia ref 和 localStorage 中
 *            页面刷新后从 localStorage 恢复状态，实现登录状态持久化
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  /**
   * 登录 Token
   * 初始化时从 localStorage 读取，用于页面刷新后保持登录状态
   */
  const token = ref(localStorage.getItem('token') || '')

  /**
   * 用户角色（中文值）：'用户' | '商家' | '管理员'
   * 初始化时从 localStorage 读取
   */
  const role = ref(localStorage.getItem('role') || '')

  /**
   * 计算属性：用户是否已登录
   * 根据 token 是否存在判断
   * @returns {boolean}
   */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 登录：保存 token 和 role 到 store 和 localStorage
   * 调用时机：Login.vue 登录成功后
   * @param {Object} data - 登录接口返回的数据 { token: string, role: string }
   */
  function login(data) {
    token.value = data.token
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
  }

  /**
   * 登出：清除 store 和 localStorage 中的登录信息
   * 调用时机：App.vue 退出登录按钮点击
   */
  function logout() {
    token.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('role')
  }

  return { token, role, isLoggedIn, login, logout }
})
