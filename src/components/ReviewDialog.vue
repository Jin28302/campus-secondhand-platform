<template>
  <el-dialog v-model="visible" title="订单评价" width="500px" @close="handleClose">
    <el-divider content-position="left">商品评价</el-divider>
    <el-form ref="productFormRef" :model="productForm" :rules="rules" label-width="80px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="productForm.rating" />
      </el-form-item>
      <el-form-item label="评价内容" prop="content">
        <el-input v-model="productForm.content" type="textarea" :rows="3" placeholder="评价商品" />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">买家评价</el-divider>
    <el-form ref="buyerFormRef" :model="buyerForm" :rules="rules" label-width="80px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="buyerForm.rating" />
      </el-form-item>
      <el-form-item label="评价内容" prop="content">
        <el-input v-model="buyerForm.content" type="textarea" :rows="3" placeholder="评价买家" />
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
import { createProductRating, createBuyerRating } from '@/api/review'

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const productFormRef = ref()
const buyerFormRef = ref()
let currentOrderId = ''
let currentProductId = ''
let currentBuyerId = ''

const productForm = reactive({ rating: 5, content: '' })
const buyerForm = reactive({ rating: 5, content: '' })

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
}

function open(orderId, productId, buyerId) {
  currentOrderId = orderId
  currentProductId = productId
  currentBuyerId = buyerId
  productForm.rating = 5
  productForm.content = ''
  buyerForm.rating = 5
  buyerForm.content = ''
  visible.value = true
}

function handleClose() {
  productFormRef.value?.resetFields()
  buyerFormRef.value?.resetFields()
}

async function handleSubmit() {
  const [v1, v2] = await Promise.all([
    productFormRef.value.validate().catch(() => false),
    buyerFormRef.value.validate().catch(() => false),
  ])
  if (!v1 || !v2) return

  loading.value = true
  try {
    await Promise.all([
      createProductRating({ orderId: currentOrderId, productId: currentProductId, rating: productForm.rating, content: productForm.content }),
      createBuyerRating({ orderId: currentOrderId, buyerId: currentBuyerId, rating: buyerForm.rating, content: buyerForm.content }),
    ])
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
