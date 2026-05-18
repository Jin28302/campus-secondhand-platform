<template>
  <el-dialog v-model="visible" :title="title" width="460px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="form.rating" />
      </el-form-item>
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入评价内容"
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
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  type: { type: String, default: 'product' },
})

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref()
let targetId = ''

const form = reactive({ rating: 5, content: '' })

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
}

const title = computed(() => (props.type === 'product' ? '商品评价' : '买家评价'))

function open(id) {
  targetId = id
  form.rating = 5
  form.content = ''
  visible.value = true
}

function handleClose() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const url = props.type === 'product' ? '/review/product' : '/review/merchant'
    const payload =
      props.type === 'product'
        ? { orderId: targetId, rating: form.rating, content: form.content }
        : { buyerOrderId: targetId, rating: form.rating, content: form.content }
    await request.post(url, payload)
    ElMessage.success('评价成功')
    visible.value = false
    emit('success')
  } catch (err) {
    ElMessage.error(err?.message || '评价失败')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
