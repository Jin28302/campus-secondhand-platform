<!--
  退货申请弹窗 - 用户提交退货申请
  主要功能：输入退货原因并提交到后端
  使用方式：父组件通过 ref 调用 open(orderId) 打开弹窗，提交后触发 success 事件
  涉及接口：PUT /api/order/:orderId/return（退货申请）
  Events: success - 退货申请成功时触发
  Exposed: open(orderId) - 打开弹窗并传入订单ID
-->
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
/**
 * 退货申请弹窗组件
 * 通过 defineExpose 暴露 open 方法供父组件调用
 * 提交成功后触发 success 事件通知父组件刷新列表
 */
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

/** 提交成功事件 */
const emit = defineEmits(['success'])

/** 弹窗可见性 */
const visible = ref(false)
/** 提交按钮 loading 状态 */
const loading = ref(false)
/** 表单组件引用 */
const formRef = ref()
/** 当前操作的订单 ID（由 open 方法传入） */
let currentOrderId = ''

/** 退货申请表单 */
const form = reactive({ reason: '' })

/** 表单校验规则：退货原因必填 */
const rules = {
  reason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }],
}

/**
 * 打开弹窗（供父组件调用）
 * 重置表单和订单 ID
 * @param {string|number} orderId - 订单ID
 */
function open(orderId) {
  currentOrderId = orderId
  visible.value = true
  form.reason = ''
}

/**
 * 关闭弹窗时重置表单校验状态
 */
function handleClose() {
  formRef.value?.resetFields()
}

/**
 * 提交退货申请
 * 校验表单 → 调用退货接口 → 成功后关闭弹窗并触发 success 事件
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await request.put(`/order/${currentOrderId}/return`, { reason: form.reason })
    ElMessage.success('退货申请已提交')
    visible.value = false
    emit('success')
  } catch (err) {
    ElMessage.error(err?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

/** 暴露 open 方法供父组件调用 */
defineExpose({ open })
</script>
