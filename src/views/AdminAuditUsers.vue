<template>
  <div v-loading="loading" class="audit-users-page">
    <h2>用户审核</h2>

    <el-table :data="users" stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="role" label="注册类型" width="100">
        <template #default="{ row }">
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

    <div v-if="users.length === 0 && !loading" class="empty">
      <el-empty description="暂无待审核用户" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const users = ref([])

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/admin/pending-users')
    users.value = res.list || []
  } catch {
    ElMessage.error('获取待审核用户失败')
  } finally {
    loading.value = false
  }
}

async function audit(user, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${user.name}」？`, '审核确认', {
      type: result === 'approved' ? 'success' : 'warning',
    })
    await request.put('/admin/audit-user', { userId: user.id, result })
    ElMessage.success(`已${action}`)
    users.value = users.value.filter((u) => u.id !== user.id)
  } catch {}
}

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
