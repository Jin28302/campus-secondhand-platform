<!--
  个人中心页 - 查看和编辑个人信息、管理钱包和积分
  主要功能：资料编辑（保存/取消）、钱包余额查看和充值、积分明细表格
  涉及接口：GET /api/wallet（钱包+用户信息）、GET /api/points/log（积分记录）、
            PUT /api/user/profile（更新资料）、POST /api/wallet/recharge（充值）
  性别映射：前端 male/female → 后端 1/0
-->
<template>
  <div v-loading="loading" class="profile-page">
    <h2>个人中心</h2>

    <div class="profile-grid">
      <!-- 左栏：个人信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <!-- 编辑/保存/取消按钮 -->
            <el-button v-if="!editing" link type="primary" @click="editing = true">编辑</el-button>
            <div v-else class="edit-actions">
              <el-button link type="primary" :loading="saving" @click="saveProfile">保存</el-button>
              <el-button link @click="cancelEdit">取消</el-button>
            </div>
          </div>
        </template>

        <!-- 个人信息表单（编辑模式下输入框可用） -->
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
        <!-- 钱包卡片：余额展示 + 充值按钮 -->
        <el-card class="wallet-card">
          <template #header>钱包</template>
          <div class="stat-value">¥{{ Number(wallet.balance).toFixed(2) }}</div>
          <p class="stat-label">账户余额</p>
          <el-button type="primary" size="small" style="margin-top:12px" @click="rechargeVisible = true">充值</el-button>
        </el-card>

        <!-- 积分卡片：当前积分展示 -->
        <el-card class="points-card">
          <template #header>积分</template>
          <div class="stat-value">{{ points.total }}</div>
          <p class="stat-label">当前积分</p>
        </el-card>
      </div>
    </div>

    <!-- 积分明细表格 -->
    <el-card class="points-detail-card">
      <template #header>积分明细</template>

      <el-table :data="points.records" stripe>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="amount" label="积分变动" width="120">
          <template #default="{ row }">
            <!-- 正积分显示绿色，负积分/消耗显示红色 -->
            <span :class="row.amount > 0 ? 'positive' : 'negative'">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
    </el-card>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeVisible" title="钱包充值" width="360px">
      <el-form label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="0.01" :precision="2" controls-position="right" style="width:200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 个人中心页 - 用户个人信息、钱包和积分管理
 * 页面挂载时并行获取钱包/积分/用户数据
 * 编辑资料时保存快照，取消编辑可恢复
 * 性别字段：前端 male/female → 后端 1/0
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

/** 页面加载状态 */
const loading = ref(false)
/** 是否处于编辑模式 */
const editing = ref(false)
/** 保存按钮 loading */
const saving = ref(false)
/** 充值弹窗可见性 */
const rechargeVisible = ref(false)
/** 充值按钮 loading */
const recharging = ref(false)
/** 充值金额，默认 100 元 */
const rechargeAmount = ref(100)

/** 用户表单数据（双向绑定） */
const userForm = reactive({
  name: '',       // 姓名
  phone: '',      // 手机号
  email: '',      // 邮箱
  city: '',       // 城市
  gender: 'male', // 性别：male/female
})
/** 编辑前的用户数据快照，用于取消编辑时恢复 */
let userSnapshot = {}

/** 钱包余额 */
const wallet = reactive({ balance: '0.00' })
/** 积分数据：总额 + 记录列表 */
const points = reactive({ total: 0, records: [] })

/**
 * 并行获取钱包、积分和用户数据
 * 自动检测后端返回字段名（兼容 camelCase 和下划线格式）
 */
async function fetchData() {
  loading.value = true
  try {
    const [walletRes, pointsRes] = await Promise.all([
      request.get('/wallet'),
      request.get('/points/log'),
    ])
    // dataAdapter 会将 wallet → wallet_balance，这里兼容两种字段名
    Object.assign(wallet, { balance: walletRes.walletBalance ?? walletRes.wallet ?? '0.00' })
    // /points/log 返回 IPage，积分记录在 .records 字段
    points.total = walletRes.points ?? 0
    points.records = pointsRes.records || []

    // 如果后端一并返回 user 信息，初始化表单
    if (walletRes.user) {
      const u = walletRes.user
      Object.assign(userForm, { ...u, gender: u.gender === 1 ? 'male' : 'female' })
      userSnapshot = { ...userForm }
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理钱包充值
 * 调用充值接口 → 成功后刷新数据
 */
async function handleRecharge() {
  recharging.value = true
  try {
    await request.post('/wallet/recharge', { amount: rechargeAmount.value })
    ElMessage.success(`成功充值 ¥${rechargeAmount.value.toFixed(2)}`)
    rechargeVisible.value = false
    // 充值后刷新数据
    fetchData()
  } catch (err) {
    ElMessage.error(err?.message || '充值失败')
  } finally {
    recharging.value = false
  }
}

/**
 * 取消编辑
 * 恢复编辑前保存的快照数据
 */
function cancelEdit() {
  Object.assign(userForm, userSnapshot)
  editing.value = false
}

/**
 * 保存用户资料
 * 将性别转为后端数值格式（男=1，女=0）后提交
 * 成功后更新快照
 */
async function saveProfile() {
  saving.value = true
  try {
    await request.put('/user/profile', {
      name: userForm.name,
      phone: userForm.phone,
      email: userForm.email,
      city: userForm.city,
      gender: userForm.gender === 'male' ? 1 : 0, // 性别转后端数值格式
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

/** 页面挂载时加载数据 */
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
