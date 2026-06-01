<!--
  评价弹窗 - 支持商品评价和买家评价两种类型
  主要功能：星级评分（1-5）+ 文字评价内容
  使用方式：父组件通过 ref 调用 open(orderId, productId, buyerId, type) 打开弹窗
           type='product' 评价商品，type='merchant' 商家评价买家
  涉及接口：POST /api/rating/seller（商品评价）、POST /api/rating/buyer（买家评价）
  Events: success - 评价成功时触发
  Exposed: open(orderId, productId, buyerId, type) - 打开弹窗
-->
<template>
  <el-dialog v-model="visible" title="订单评价" width="500px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <!-- 星级评分（1-5 星） -->
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="form.rating" />
      </el-form-item>
      <!-- 评价内容（文本域） -->
      <el-form-item label="评价内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="3"
          :placeholder="currentType === 'product' ? '评价商品' : '评价买家'" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 评价弹窗组件
 * 支持商品评价(product)和买家评价(merchant)两种类型
 * 根据 type 参数调用不同的后端接口
 */
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createProductRating, createBuyerRating } from '@/api/review'

/** 提交成功事件 */
const emit = defineEmits(['success'])

/** 弹窗可见性 */
const visible = ref(false)
/** 提交按钮 loading 状态 */
const loading = ref(false)
/** 表单组件引用 */
const formRef = ref()
/** 评价相关的订单 ID */
let currentOrderId = ''
/** 评价相关的商品 ID（商品评价时需要） */
let currentProductId = ''
/** 评价相关的买家 ID（买家评价时需要） */
let currentBuyerId = ''
/** 评价类型：'product'（商品评价）或 'merchant'（买家评价） */
let currentType = 'product'

/** 评价表单：评分默认 5 星，内容为空 */
const form = reactive({ rating: 5, content: '' })

/** 表单校验规则：评分和内容均必填 */
const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
}

/**
 * 打开弹窗（供父组件调用）
 * 根据 type 参数决定评价类型和后续调用的接口
 * @param {string|number} orderId - 订单ID
 * @param {string|number|null} productId - 商品ID（商品评价时需要）
 * @param {string|number|null} buyerId - 买家ID（买家评价时需要）
 * @param {string} type - 评价类型：'product' 或 'merchant'，默认 'product'
 */
function open(orderId, productId, buyerId, type = 'product') {
  currentOrderId = orderId
  currentProductId = productId
  currentBuyerId = buyerId
  currentType = type
  form.rating = 5
  form.content = ''
  visible.value = true
}

/**
 * 关闭弹窗时重置表单校验状态
 */
function handleClose() {
  formRef.value?.resetFields()
}

/**
 * 提交评价
 * 校验表单 → 根据评价类型调用不同的后端接口
 * 成功后关闭弹窗并触发 success 事件
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (currentType === 'product') {
      // 商品评价：调用 /rating/seller 接口
      await createProductRating({ orderId: currentOrderId, productId: currentProductId, rating: form.rating, content: form.content })
    } else {
      // 买家评价：调用 /rating/buyer 接口
      await createBuyerRating({ orderId: currentOrderId, buyerId: currentBuyerId, rating: form.rating, content: form.content })
    }
    ElMessage.success('评价成功')
    visible.value = false
    emit('success')
  } catch (err) {
    ElMessage.error(err?.message || '评价失败')
  } finally {
    loading.value = false
  }
}

/** 暴露 open 方法供父组件调用 */
defineExpose({ open })
</script>
