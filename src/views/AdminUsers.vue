<!--
  管理员-用户管理页 - 管理所有用户
  主要功能：查看所有用户列表、修改用户信息（含角色）、删除用户
  涉及接口：GET /api/admin/users（所有用户）、PUT /api/admin/user/:id（修改用户）、DELETE /api/admin/user/:id（删除用户）
  角色限制：仅管理员可访问
-->
<template>
  <div v-loading="loading" class="admin-users-page">
    <h2>用户管理</h2>

    <!-- 所有用户列表 -->
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

    <!-- 修改用户弹窗 -->
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
        <!-- 管理员可修改用户角色：用户/商家/管理员 -->
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
/**
 * 管理员-用户管理页 - 管理所有注册用户
 * 支持修改用户基本信息、更换角色，以及删除用户
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** 页面加载状态 */
const loading = ref(false)
/** 所有用户列表 */
const users = ref([])
/** 编辑弹窗可见性 */
const editVisible = ref(false)
/** 保存按钮 loading */
const saving = ref(false)
/** 表单组件引用 */
const formRef = ref()
/** 当前编辑的用户 ID */
let currentUserId = ''

/** 编辑表单数据 */
const editForm = reactive({ name: '', phone: '', email: '', city: '', role: 'user' })

/** 表单校验规则 */
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
}

/**
 * 角色中文映射
 * @param {string} role - 角色值
 * @returns {string} 中文角色名
 */
function roleLabel(role) {
  const map = { user: '用户', merchant: '商家', admin: '管理员' }
  return map[role] || role
}

/**
 * 获取所有用户列表
 * 调用管理员获取所有用户的接口
 */
async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/admin/users')
    users.value = res.records || []
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 打开编辑弹窗
 * 将选中的用户数据填充到表单
 * @param {Object} row - 用户行数据
 */
function openEdit(row) {
  currentUserId = row.id
  Object.assign(editForm, { name: row.name, phone: row.phone, email: row.email, city: row.city, role: row.role })
  editVisible.value = true
}

/** 重置编辑表单 */
function resetForm() {
  formRef.value?.resetFields()
}

/**
 * 保存用户修改
 * 校验表单后调用管理员更新用户接口
 */
async function saveUser() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await request.put(`/admin/user/${currentUserId}`, editForm)
    ElMessage.success('修改成功')
    editVisible.value = false
    fetchUsers()
  } catch (err) {
    ElMessage.error(err?.message || '修改失败')
  } finally {
    saving.value = false
  }
}

/**
 * 删除用户（不可恢复）
 * 弹出确认对话框 → 调用删除接口 → 从列表移除
 * @param {Object} row - 用户行数据
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.name}」？此操作不可恢复。`, '删除确认', { type: 'error' })
    await request.delete(`/admin/user/${row.id}`)
    ElMessage.success('已删除')
    // 前端移除该用户
    users.value = users.value.filter((u) => u.id !== row.id)
  } catch {}
}

/** 页面挂载时加载用户列表 */
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
