<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>注册</h2>
          <el-radio-group v-model="userType" @change="onTypeChange">
            <el-radio-button value="user">普通用户</el-radio-button>
            <el-radio-button value="merchant">商家</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>

        <template v-if="userType === 'merchant'">
          <el-form-item label="营业执照" prop="businessLicense">
            <ImageUpload v-model="form.businessLicense" />
          </el-form-item>
          <el-form-item label="身份证" prop="idCard">
            <ImageUpload v-model="form.idCard" />
          </el-form-item>
        </template>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 180px" />
            <img :src="captchaUrl" class="captcha-img" alt="验证码" @click="refreshCaptcha" />
          </div>
          <el-button link type="primary" style="padding-left:0;margin-top:4px" @click="refreshCaptcha">刷新验证码</el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">注册</el-button>
          <el-button @click="$router.push('/login')">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, registerUser, registerMerchant } from '@/api/auth'
import ImageUpload from '@/components/ImageUpload.vue'

const captchaAnswers = { 1: 'K7mX4', 2: 'A93Pw', 3: '5RnQ8', 4: 'T2H6j', 5: 'G1Zv0' }
let currentCaptchaIdx = 1

const router = useRouter()
const formRef = ref()
const userType = ref('user')
const loading = ref(false)
const captchaUrl = ref('')

const form = reactive({
  name: '',
  phone: '',
  email: '',
  city: '',
  gender: 'male',
  bankAccount: '',
  password: '',
  confirmPassword: '',
  businessLicense: null,
  idCard: null,
  captcha: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  bankAccount: [{ required: true, message: '请输入银行账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== captchaAnswers[currentCaptchaIdx]) callback(new Error('验证码错误，注意大小写'))
        else callback()
      }, trigger: 'blur' }],
})

function onTypeChange() {
  form.businessLicense = null
  form.idCard = null
}

async function refreshCaptcha() {
  currentCaptchaIdx = Math.floor(Math.random() * 5) + 1
  captchaUrl.value = `/captcha/${currentCaptchaIdx}.svg?t=${Date.now()}`
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (userType.value === 'merchant') {
    if (!form.businessLicense) return ElMessage.warning('请上传营业执照')
    if (!form.idCard) return ElMessage.warning('请上传身份证')
  }

  loading.value = true
  try {
    const data = {
      name: form.name,
      phone: form.phone,
      email: form.email,
      city: form.city,
      gender: form.gender,
      bankAccount: form.bankAccount,
      password: form.password,
      captcha: form.captcha,
    }

    if (userType.value === 'merchant') {
      await registerMerchant({ ...data, businessLicense: form.businessLicense, idCard: form.idCard })
    } else {
      await registerUser(data)
    }

    ElMessage.success('注册成功')
    router.push('/login')
  } catch (err) {
    ElMessage.error(err?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.register-card {
  width: 560px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
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
