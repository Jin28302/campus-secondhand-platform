<template>
  <div v-loading="loading" class="profile-page">
    <h2>个人中心</h2>

    <div class="profile-grid">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <el-button v-if="!editing" link type="primary" @click="editing = true">编辑</el-button>
            <div v-else class="edit-actions">
              <el-button link type="primary" :loading="saving" @click="saveProfile">保存</el-button>
              <el-button link @click="cancelEdit">取消</el-button>
            </div>
          </div>
        </template>

        <el-form :model="userForm" label-width="80px">
          <el-form-item label="姓名">
            <el-input v-model="userForm.name" :disabled="!editing" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userForm.phone" :disabled="!editing" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userForm.email" :disabled="!editing" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="userForm.city" :disabled="!editing" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="userForm.gender" :disabled="!editing">
              <el-radio value="male">男</el-radio>
              <el-radio value="female">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="right-col">
        <el-card class="wallet-card">
          <template #header>钱包</template>
          <div class="stat-value">¥{{ wallet.balance }}</div>
          <p class="stat-label">账户余额</p>
        </el-card>

        <el-card class="points-card">
          <template #header>积分</template>
          <div class="stat-value">{{ points.total }}</div>
          <p class="stat-label">当前积分</p>
        </el-card>
      </div>
    </div>

    <el-card class="points-detail-card">
      <template #header>积分明细</template>

      <el-table :data="points.records" stripe>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="amount" label="积分变动" width="120">
          <template #default="{ row }">
            <span :class="row.amount > 0 ? 'positive' : 'negative'">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const editing = ref(false)
const saving = ref(false)

const userForm = reactive({
  name: '',
  phone: '',
  email: '',
  city: '',
  gender: 'male',
})
let userSnapshot = {}

const wallet = reactive({ balance: '0.00' })
const points = reactive({ total: 0, records: [] })

async function fetchData() {
  loading.value = true
  try {
    const [walletRes, pointsRes] = await Promise.all([
      request.get('/wallet'),
      request.get('/points'),
    ])
    Object.assign(wallet, { balance: walletRes.balance || '0.00' })
    points.total = pointsRes.total || 0
    points.records = pointsRes.records || []

    if (walletRes.user) {
      Object.assign(userForm, walletRes.user)
      userSnapshot = { ...walletRes.user }
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

function cancelEdit() {
  Object.assign(userForm, userSnapshot)
  editing.value = false
}

async function saveProfile() {
  saving.value = true
  try {
    await request.put('/user', {
      name: userForm.name,
      phone: userForm.phone,
      email: userForm.email,
      city: userForm.city,
      gender: userForm.gender,
    })
    userSnapshot = { ...userForm }
    editing.value = false
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.profile-page h2 {
  margin: 0 0 20px;
}

.profile-grid {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.info-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.right-col {
  width: 240px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wallet-card,
.points-card {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}

.points-detail-card {
  margin-top: 0;
}

.positive {
  color: #67c23a;
  font-weight: bold;
}

.negative {
  color: #f56c6c;
  font-weight: bold;
}
</style>
