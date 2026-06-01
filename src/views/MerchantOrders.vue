<!--
  商家订单管理页 - 商家查看和管理订单
  主要功能：查看订单列表、审核退货申请（同意/拒绝）、评价买家
  涉及接口：GET /api/order/seller/list（商家订单列表）、PUT /api/return/:id/audit（审核退货）
-->
<template>
  <div v-loading="loading" class="merchant-orders-page">
    <h2>订单管理</h2>

    <!-- 订单列表表格 -->
    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div v-for="item in row.items" :key="item.id" class="order-product">
            {{ item.name }} × {{ item.quantity }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">￥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- 待发货：显示确认发货按钮 -->
          <el-button
            v-if="row.status === 'pending'"
            type="primary"
            size="small"
            link
            @click="shipOrder(row)"
          >
            确认发货
          </el-button>
          <!-- 退货待审核：显示审核退货按钮 -->
          <el-button
            v-if="row.status === 'refund_pending'"
            type="warning"
            size="small"
            link
            @click="openAudit(row)"
          >
            审核退货
          </el-button>
          <!-- 已完成且商家未评价买家：显示评价按钮 -->
          <el-button
            v-if="row.status === 'completed' && !row.merchantReviewed"
            type="success"
            size="small"
            link
            @click="openReview(row)"
          >
            评价买家
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 评价弹窗（商家对买家评价，type='merchant'） -->
    <ReviewDialog ref="reviewRef" type="merchant" @success="fetchOrders" />

    <!-- 退货审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核退货" width="440px">
      <div class="audit-info">
        <p><strong>订单号：</strong>{{ currentOrder?.orderNo }}</p>
        <p><strong>退货原因：</strong>{{ currentOrder?.refundReason }}</p>
      </div>

      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditResult">
            <el-radio value="approved">同意退货</el-radio>
            <el-radio value="rejected">拒绝退货</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 拒绝时必须填写原因 -->
        <el-form-item v-if="auditResult === 'rejected'" label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 商家订单管理页 - 商家查看订单并审核退货、评价买家
 * 退货审核：同意/拒绝，拒绝时需填写原因
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'

/** 页面加载状态 */
const loading = ref(false)
/** 订单列表 */
const orders = ref([])
/** 退货审核弹窗可见性 */
const auditVisible = ref(false)
/** 审核按钮 loading */
const auditing = ref(false)
/** 当前审核的订单 */
const currentOrder = ref(null)
/** 审核结果：approved(同意) / rejected(拒绝) */
const auditResult = ref('approved')
/** 拒绝原因（拒绝时必填） */
const rejectReason = ref('')
/** 评价弹窗组件引用 */
const reviewRef = ref()

/**
 * 打开评价买家弹窗
 * type='merchant' 表示商家对买家评价
 * @param {Object} row - 订单行数据
 */
function openReview(row) {
  reviewRef.value.open(row.id, null, row.buyerId, 'merchant')
}

/**
 * 订单状态中文映射
 * @param {string} status - 后端状态值
 * @returns {string} 中文状态描述
 */
function statusLabel(status) {
  const map = {
    pending: '待发货',
    shipped: '待收货',
    completed: '已完成',
    refund_pending: '退货待审核',
    refunded: '已退货',
    refund_rejected: '退货被拒',
  }
  return map[status] || status
}

function statusTagType(status) {
  const map = {
    pending: '',
    shipped: 'warning',
    completed: 'success',
    refund_pending: 'danger',
    refunded: 'info',
    refund_rejected: '',
  }
  return map[status] || ''
}

/**
 * 商家确认发货 - 将订单从待发货变为待收货
 * @param {Object} row - 订单数据
 */
async function shipOrder(row) {
  try {
    await ElMessageBox.confirm(`确认已发出该订单的商品？`, '确认发货', { type: 'info' })
    await request.put(`/order/${row.id}/ship`)
    ElMessage.success('已确认发货')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err?.msg || err?.message || '发货失败')
  }
}

/**
 * 获取商家订单列表
 * 调用商家专属的订单列表接口
 */
async function fetchOrders() {
  loading.value = true
  try {
    const res = await request.get('/order/seller/list')
    orders.value = res.records || []
  } catch {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

/**
 * 打开退货审核弹窗
 * 重置审核状态和拒绝原因
 * @param {Object} order - 订单对象
 */
function openAudit(order) {
  currentOrder.value = order
  auditResult.value = 'approved'
  rejectReason.value = ''
  auditVisible.value = true
}

/**
 * 提交退货审核结果
 * 拒绝时原因必填；审核完成后刷新订单列表
 */
async function submitAudit() {
  // 拒绝退货时原因必填
  if (auditResult.value === 'rejected' && !rejectReason.value.trim()) {
    return ElMessage.warning('请输入拒绝原因')
  }

  auditing.value = true
  try {
    await request.put(`/return/order/${currentOrder.value.id}/audit`, {
      result: auditResult.value === 'approved' ? '同意' : '拒绝',
      rejectReason: auditResult.value === 'rejected' ? rejectReason.value : undefined,
    })
    ElMessage.success('审核完成')
    auditVisible.value = false
    fetchOrders()
  } catch (err) {
    ElMessage.error(err?.message || '审核失败')
  } finally {
    auditing.value = false
  }
}

/** 页面挂载时加载订单列表 */
onMounted(fetchOrders)
</script>

<style scoped>
.merchant-orders-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.merchant-orders-page h2 {
  margin: 0 0 16px;
}

.order-product {
  font-size: 13px;
  line-height: 1.6;
}

.audit-info p {
  margin: 6px 0;
}
</style>
