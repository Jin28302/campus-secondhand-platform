<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 180px" />
            <img :src="captchaUrl" class="captcha-img" alt="验证码" @click="refreshCaptcha" />
          </div>
          <el-button link type="primary" style="padding-left:0;margin-top:4px" @click="refreshCaptcha">刷新验证码</el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button @click="$router.push('/register')">去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const captchaUrl = ref('')

const form = reactive({
  phone: '',
  captcha: '',
})

const captchaAnswers = { 1: 'K7mX4', 2: 'A93Pw', 3: '5RnQ8', 4: 'T2H6j', 5: 'G1Zv0' }
let currentCaptchaIdx = 1

const validateCaptcha = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入验证码'))
  if (value !== captchaAnswers[currentCaptchaIdx]) return callback(new Error('验证码错误，注意大小写'))
  callback()
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  captcha: [{ validator: validateCaptcha, trigger: 'blur' }],
}

async function refreshCaptcha() {
  currentCaptchaIdx = Math.floor(Math.random() * 5) + 1
  captchaUrl.value = `/captcha/${currentCaptchaIdx}.svg?t=${Date.now()}`
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/login', form)
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    ElMessage.error(err?.message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

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
