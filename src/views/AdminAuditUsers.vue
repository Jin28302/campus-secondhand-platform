<!--
  管理员-用户审核页 - 审核新注册用户（含商家）
  主要功能：查看待审核用户列表、通过/拒绝审核
  涉及接口：GET /api/admin/users/pending（待审核用户列表）、PUT /api/admin/user/:id/audit（审核用户）
-->
<template>
  <div v-loading="loading" class="audit-users-page">
    <h2>用户审核</h2>

    <!-- 待审核用户列表 -->
    <el-table :data="users" stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="role" label="注册类型" width="100">
        <template #default="{ row }">
          <!-- 商家标记为 warning 色，用户为默认色 -->
          <el-tag :type="row.role === 'merchant' ? 'warning' : ''" size="small">
            {{ row.role === 'merchant' ? '商家' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" link @click="audit(row, 'approved')">
            通过
          </el-button>
          <el-button type="danger" size="small" link @click="audit(row, 'rejected')">
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="users.length === 0 && !loading" class="empty">
      <el-empty description="暂无待审核用户" />
    </div>
  </div>
</template>

<script setup>
/**
 * 管理员-用户审核页 - 审核待审核的用户
 * 审核通过/拒绝后前端从列表中移除
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** 页面加载状态 */
const loading = ref(false)
/** 待审核用户列表 */
const users = ref([])

/**
 * 获取待审核用户列表
 * 调用管理员接口获取 status=pending 的用户
 */
async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/admin/users/pending')
    users.value = res || []
  } catch {
    ElMessage.error('获取待审核用户失败')
  } finally {
    loading.value = false
  }
}

/**
 * 审核用户（通过或拒绝）
 * 弹出确认对话框 → 调用审核接口 → 从列表中移除
 * @param {Object} user - 用户对象
 * @param {string} result - 审核结果：'approved'（通过）或 'rejected'（拒绝）
 */
async function audit(user, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${user.name}」？`, '审核确认', {
      type: result === 'approved' ? 'success' : 'warning',
    })
    await request.put(`/admin/user/${user.id}/audit`, { result: action })
    ElMessage.success(`已${action}`)
    // 审核后从列表中移除该用户
    users.value = users.value.filter((u) => u.id !== user.id)
  } catch {}
}

/** 页面挂载时获取待审核用户 */
onMounted(fetchUsers)
</script>

<style scoped>
.audit-users-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.audit-users-page h2 {
  margin: 0 0 16px;
}

.empty {
  padding: 60px 0;
}
</style>
