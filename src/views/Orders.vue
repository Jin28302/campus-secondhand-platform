<template>
  <div v-loading="loading" class="orders-page">
    <h2>我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="待收货" name="pending" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已退货" name="refunded" />
    </el-tabs>

    <div v-if="orders.length === 0 && !loading" class="empty">
      <el-empty description="暂无订单" />
    </div>

    <div v-for="order in orders" :key="order.id" class="order-card">
      <div class="order-header">
        <span class="order-no">订单号：{{ order.orderNo }}</span>
        <span class="order-date">{{ order.createdAt }}</span>
        <el-tag :type="statusTagType(order.status)" size="small">
          {{ statusLabel(order.status) }}
        </el-tag>
      </div>

      <div v-for="item in order.items" :key="item.id" class="order-item">
        <img :src="item.image" :alt="item.name" class="item-img" />
        <div class="item-info">
          <p class="item-name">{{ item.name }}</p>
          <p class="item-meta">¥{{ item.price }} × {{ item.quantity }}</p>
        </div>
      </div>

      <div class="order-footer">
        <span class="order-total">合计：¥{{ order.totalAmount }}</span>
        <div class="order-actions">
          <el-button
            v-if="order.status === 'pending'"
            type="primary"
            size="small"
            @click="confirmReceive(order)"
          >
            确认收货
          </el-button>
          <el-button
            v-if="canRefund(order)"
            type="warning"
            size="small"
            @click="requestRefund(order)"
          >
            申请退货
          </el-button>
          <el-button
            v-if="order.status === 'completed' && !order.reviewed"
            type="success"
            size="small"
            @click="openReview(order)"
          >
            评价
          </el-button>
          <el-tag v-if="order.status === 'completed' && order.reviewed" type="info" size="small">已评价</el-tag>
        </div>
      </div>
    </div>

    <ReviewDialog ref="reviewRef" type="product" @success="fetchOrders" />
    <RefundDialog ref="refundRef" @success="fetchOrders" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'
import RefundDialog from '@/components/RefundDialog.vue'

const loading = ref(false)
const activeTab = ref('pending')
const orders = ref([])
const reviewRef = ref()
const refundRef = ref()

function openReview(order) {
  const productId = order.items?.[0]?.productId
  reviewRef.value.open(order.id, productId, order.buyerId)
}

function statusLabel(status) {
  const map = { pending: '待收货', completed: '已完成', refunded: '已退货', refund_pending: '退货待审核', refund_rejected: '退货被拒' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'warning', completed: 'success', refunded: 'info', refund_pending: 'danger', refund_rejected: '' }
  return map[status] || ''
}

function canRefund(order) {
  if (order.status !== 'completed') return false
  const created = new Date(order.createdAt).getTime()
  const now = Date.now()
  return now - created < 24 * 60 * 60 * 1000
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await request.get('/orders', { params: { status: activeTab.value } })
    orders.value = res.list || []
  } catch {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

async function confirmReceive(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await request.put('/order/confirm', { orderId: order.id })
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch {}
}

function requestRefund(order) {
  refundRef.value.open(order.id)
}

onMounted(fetchOrders)
</script>

<style scoped>
.orders-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.orders-page h2 {
  margin: 0 0 16px;
}

.empty {
  padding: 60px 0;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.order-no {
  font-weight: bold;
}

.order-date {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.item-img {
  width: 52px;
  height: 52px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 4px;
}

.item-meta {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.order-total {
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
