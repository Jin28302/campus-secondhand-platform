<template>
  <el-dialog v-model="visible" title="申请退货" width="420px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="退货原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入退货原因"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref()
let currentOrderId = ''

const form = reactive({ reason: '' })

const rules = {
  reason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }],
}

function open(orderId) {
  currentOrderId = orderId
  visible.value = true
  form.reason = ''
}

function handleClose() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await request.post('/refund/request', { orderId: currentOrderId, reason: form.reason })
    ElMessage.success('退货申请已提交')
    visible.value = false
    emit('success')
  } catch (err) {
    ElMessage.error(err?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
