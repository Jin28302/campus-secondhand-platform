<template>
  <div v-loading="loading" class="admin-users-page">
    <h2>用户管理</h2>

    <el-table :data="users" stripe>
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEdit(row)">修改</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editVisible" title="修改用户" width="460px" @close="resetForm">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="editForm.city" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role">
            <el-option label="用户" value="user" />
            <el-option label="商家" value="merchant" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const users = ref([])
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
let currentUserId = ''

const editForm = reactive({ name: '', phone: '', email: '', city: '', role: 'user' })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
}

function roleLabel(role) {
  const map = { user: '用户', merchant: '商家', admin: '管理员' }
  return map[role] || role
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/admin/users')
    users.value = res.list || []
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  currentUserId = row.id
  Object.assign(editForm, { name: row.name, phone: row.phone, email: row.email, city: row.city, role: row.role })
  editVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function saveUser() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await request.put('/admin/user', { userId: currentUserId, ...editForm })
    ElMessage.success('修改成功')
    editVisible.value = false
    fetchUsers()
  } catch (err) {
    ElMessage.error(err?.message || '修改失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.name}」？此操作不可恢复。`, '删除确认', { type: 'error' })
    await request.delete('/admin/user', { data: { userId: row.id } })
    ElMessage.success('已删除')
    users.value = users.value.filter((u) => u.id !== row.id)
  } catch {}
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-users-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.admin-users-page h2 {
  margin: 0 0 16px;
}
</style>
