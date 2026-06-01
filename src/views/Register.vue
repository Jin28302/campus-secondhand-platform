<!--
  注册页面 - 新用户注册入口
  主要功能：支持普通用户和商家两种角色注册，商家需要额外上传营业执照和身份证
  涉及接口：POST /api/register（注册 - 区分用户/商家角色）
-->
<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>注册</h2>
          <!-- 注册类型切换：普通用户 / 商家 -->
          <el-radio-group v-model="userType" @change="onTypeChange">
            <el-radio-button value="user">普通用户</el-radio-button>
            <el-radio-button value="merchant">商家</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 基本信息：姓名、手机号、邮箱、城市、性别、银行账号 -->
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

        <!-- 商家注册需额外上传营业执照和身份证 -->
        <template v-if="userType === 'merchant'">
          <el-form-item label="营业执照" prop="businessLicense">
            <ImageUpload v-model="form.businessLicense" />
          </el-form-item>
          <el-form-item label="身份证" prop="idCard">
            <ImageUpload v-model="form.idCard" />
          </el-form-item>
        </template>

        <!-- 验证码输入（开发环境用本地 SVG 模拟） -->
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 180px" />
            <img :src="captchaUrl" class="captcha-img" alt="验证码" @click="refreshCaptcha" />
          </div>
          <el-button link type="primary" style="padding-left:0;margin-top:4px" @click="refreshCaptcha">刷新验证码</el-button>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">注册</el-button>
          <el-button @click="$router.push('/login')">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 注册页面 - 支持普通用户和商家两种角色注册
 * 商家注册时需要额外上传营业执照和身份证图片
 * 性别字段：前端 male/female → 后端 1/0
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerUser, registerMerchant } from '@/api/auth'
import ImageUpload from '@/components/ImageUpload.vue'

/**
 * 本地验证码答案表（开发环境模拟后端验证码校验）
 * key: 验证码编号 1-5，value: 对应的正确答案
 */
const captchaAnswers = { 1: 'K7mX4', 2: 'A93Pw', 3: '5RnQ8', 4: 'T2H6j', 5: 'G1Zv0' }
/** 当前验证码编号（1-5） */
let currentCaptchaIdx = 1

/** Vue Router 实例 */
const router = useRouter()
/** 表单组件引用 */
const formRef = ref()
/** 当前选择的用户类型：user（普通用户）或 merchant（商家） */
const userType = ref('user')
/** 注册按钮 loading 状态 */
const loading = ref(false)
/** 验证码图片 URL */
const captchaUrl = ref('')

/** 注册表单数据 */
const form = reactive({
  name: '',             // 姓名
  phone: '',            // 手机号
  email: '',            // 邮箱
  city: '',             // 城市
  gender: 'male',       // 性别：male 男 / female 女
  bankAccount: '',      // 银行账号
  password: '',         // 密码
  confirmPassword: '',  // 确认密码
  businessLicense: null,// 营业执照图片 URL（仅商家）
  idCard: null,         // 身份证图片 URL（仅商家）
  captcha: '',          // 验证码输入
})

/**
 * 确认密码校验函数
 * 自定义校验规则：确认密码必须与密码字段一致
 * @param {Object} rule - 校验规则对象
 * @param {string} value - 当前字段值
 * @param {Function} callback - 校验完成回调
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

/** 表单校验规则配置 */
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

/**
 * 切换用户类型时清空商家专属字段
 * 避免从商家切回普通用户时残留营业执照和身份证数据
 */
function onTypeChange() {
  form.businessLicense = null
  form.idCard = null
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
 * 处理注册表单提交
 * 校验表单 → 根据用户类型调用不同的注册接口 → 成功后跳转登录页
 * 商家注册前会额外检查营业执照和身份证是否已上传
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 商家注册前必须上传营业执照和身份证
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
      gender: form.gender === 'male' ? 1 : 0, // 性别转为后端数值格式：男=1，女=0
      bankAccount: form.bankAccount,
      password: form.password,
      captcha: form.captcha,
    }

    if (userType.value === 'merchant') {
      // 商家注册：附带营业执照和身份证图片
      await registerMerchant({ ...data, captchaKey: 'local', captchaCode: form.captcha, businessLicense: form.businessLicense, idCard: form.idCard })
    } else {
      // 普通用户注册
      await registerUser({ ...data, captchaKey: 'local', captchaCode: form.captcha })
    }

    ElMessage.success('注册成功')
    router.push('/login')
  } catch (err) {
    ElMessage.error(err?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

/** 页面挂载时加载验证码 */
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
