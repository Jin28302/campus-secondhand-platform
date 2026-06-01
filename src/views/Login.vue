<!--
  登录页面 - 用户登录入口
  主要功能：手机号+密码+图形验证码登录
  涉及接口：POST /api/login（登录 - 返回 token/role）
-->
<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- 手机号输入 -->
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <!-- 验证码输入（图形验证码，开发环境用本地 SVG 模拟） -->
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 180px" />
            <img :src="captchaUrl" class="captcha-img" alt="验证码" @click="refreshCaptcha" />
          </div>
          <el-button link type="primary" style="padding-left:0;margin-top:4px" @click="refreshCaptcha">刷新验证码</el-button>
        </el-form-item>

        <!-- 登录/去注册操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button @click="$router.push('/register')">去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 登录页面 - 手机号+密码+图形验证码登录
 * 登录成功后存储 token 和 role 到 localStorage 和 Pinia store
 * 支持登录后回跳到之前要访问的页面（通过 redirect query 参数）
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/** Vue Router 实例 */
const router = useRouter()
/** 当前路由对象，用于读取 redirect 查询参数 */
const route = useRoute()
/** 表单组件引用，用于表单校验 */
const formRef = ref()
/** 登录按钮 loading 状态 */
const loading = ref(false)
/** 验证码图片 URL */
const captchaUrl = ref('')

/** 登录表单数据 */
const form = reactive({
  phone: '',       // 手机号
  password: '',    // 密码
  captcha: '',     // 验证码输入
})

/**
 * 本地验证码答案表（开发环境模拟后端验证码校验）
 * key: 验证码编号 1-5，value: 对应的正确答案
 */
const captchaAnswers = { 1: 'K7mX4', 2: 'A93Pw', 3: '5RnQ8', 4: 'T2H6j', 5: 'G1Zv0' }
/** 当前验证码编号（1-5） */
let currentCaptchaIdx = 1

/**
 * 自定义验证码校验规则
 * @param {Object} rule - Element Plus 校验规则对象
 * @param {string} value - 用户输入的验证码
 * @param {Function} callback - 校验完成回调
 */
const validateCaptcha = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入验证码'))
  if (value !== captchaAnswers[currentCaptchaIdx]) return callback(new Error('验证码错误，注意大小写'))
  callback()
}

/** 表单校验规则配置 */
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  captcha: [{ validator: validateCaptcha, trigger: 'blur' }],
}

/**
 * 刷新验证码图片
 * 随机选择一个验证码编号，构造带时间戳的 URL 防止浏览器缓存
 */
async function refreshCaptcha() {
  currentCaptchaIdx = Math.floor(Math.random() * 5) + 1
  captchaUrl.value = `/captcha/${currentCaptchaIdx}.svg?t=${Date.now()}`
}

/**
 * 处理登录表单提交
 * 校验表单 → 调用登录接口 → 存储 token/role → 跳转到目标页面或首页
 * 登录失败时自动刷新验证码，防止验证码复用
 */
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/login', {
      account: form.phone,
      password: form.password,
      captchaKey: 'local',
      captchaCode: form.captcha,
    })
    // 登录成功后存储 token/role 到 store 和 localStorage
    useUserStore().login(res)
    ElMessage.success('登录成功')
    // 支持登录后跳回原来要访问的页面，默认为首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    ElMessage.error(err?.message || '登录失败')
    // 登录失败刷新验证码，防止验证码复用
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

/** 页面挂载时加载验证码 */
onMounted(refreshCaptcha)
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.login-card {
  width: 420px;
}

.login-card h2 {
  margin: 0;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-img {
  height: 36px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
