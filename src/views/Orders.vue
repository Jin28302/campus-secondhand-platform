<!--
  我的订单页 - 查看和管理用户订单
  主要功能：待收货/已完成/已退货 Tab 切换、确认收货、申请退货、商品评价
  涉及接口：GET /api/order/list?status=（订单列表）、PUT /api/order/:id/confirm（确认收货）、
            PUT /api/order/:id/return（退货申请）
-->
<template>
  <div v-loading="loading" class="orders-page">
    <h2>我的订单</h2>

    <!-- 订单状态 Tab 切换：待发货、待收货、已完成、已退货 -->
    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="待发货" name="pending" />
      <el-tab-pane label="待收货" name="shipped" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已退货" name="refunded" />
    </el-tabs>

    <div v-if="orders.length === 0 && !loading" class="empty">
      <el-empty description="暂无订单" />
    </div>

    <!-- 订单卡片列表 -->
    <div v-for="order in orders" :key="order.id" class="order-card">
      <!-- 订单头部：订单号、下单时间、状态标签 -->
      <div class="order-header">
        <span class="order-no">订单号：{{ order.orderNo }}</span>
        <span class="order-date">{{ order.createdAt }}</span>
        <el-tag :type="statusTagType(order.status)" size="small">
          {{ statusLabel(order.status) }}
        </el-tag>
      </div>

      <!-- 订单内商品列表 -->
      <div v-for="item in order.items" :key="item.id" class="order-item">
        <img :src="item.image" :alt="item.name" class="item-img" />
        <div class="item-info">
          <p class="item-name">{{ item.name }}</p>
          <p class="item-meta">￥{{ Number(item.price).toFixed(2) }} × {{ item.quantity }}</p>
        </div>
      </div>

      <!-- 订单底部：合计金额 + 操作按钮（根据状态动态显示） -->
      <div class="order-footer">
        <span class="order-total">合计：￥{{ order.totalAmount }}</span>
        <div class="order-actions">
          <!-- 待收货：确认收货按钮 -->
          <el-button
            v-if="order.status === 'shipped'"
            type="primary"
            size="small"
            @click="confirmReceive(order)"
          >
            确认收货
          </el-button>
          <!-- 已完成且在退货有效期内：申请退货按钮 -->
          <el-button
            v-if="canRefund(order)"
            type="warning"
            size="small"
            @click="requestRefund(order)"
          >
            申请退货
          </el-button>
          <!-- 已完成且未评价：评价按钮 -->
          <el-button
            v-if="order.status === 'completed' && !order.reviewed"
            type="success"
            size="small"
            @click="openReview(order)"
          >
            评价
          </el-button>
          <!-- 已完成且已评价：已评价标签 -->
          <el-tag v-if="order.status === 'completed' && order.reviewed" type="info" size="small">已评价</el-tag>
        </div>
      </div>
    </div>

    <!-- 评价弹窗和退货弹窗（通过子组件 ref 调用） -->
    <ReviewDialog ref="reviewRef" type="product" @success="fetchOrders" />
    <RefundDialog ref="refundRef" @success="fetchOrders" />
  </div>
</template>

<script setup>
/**
 * 我的订单页 - 用户查看和管理订单
 * 支持按状态切换 Tab 查看订单、确认收货、申请退货、商品评价
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'
import RefundDialog from '@/components/RefundDialog.vue'

/** 页面加载状态 */
const loading = ref(false)
/** 当前激活的 Tab：pending/completed/refunded */
const activeTab = ref('pending')
/** 订单列表数据 */
const orders = ref([])
/** 评价弹窗组件引用 */
const reviewRef = ref()
/** 退货弹窗组件引用 */
const refundRef = ref()

/**
 * 打开商品评价弹窗
 * 取订单第一个商品作为评价目标
 * @param {Object} order - 订单对象
 */
function openReview(order) {
  const productId = order.items?.[0]?.productId
  reviewRef.value.open(order.id, productId, null, 'product')
}

/**
 * 订单状态中文映射
 * @param {string} status - 后端状态值
 * @returns {string} 中文状态名
 */
function statusLabel(status) {
  const map = { pending: '待发货', shipped: '待收货', completed: '已完成', refunded: '已退货', refund_pending: '退货待审核', refund_rejected: '退货被拒' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: '', shipped: 'warning', completed: 'success', refunded: 'info', refund_pending: 'danger', refund_rejected: '' }
  return map[status] || ''
}

/**
 * 判断订单是否可以申请退货
 * 条件：订单状态为"已完成"且在退货截止日期之前
 * @param {Object} order - 订单对象
 * @returns {boolean}
 */
function canRefund(order) {
  if (order.status !== 'completed') return false
  if (!order.returnDeadline) return false
  return new Date(order.returnDeadline).getTime() > Date.now()
}

/**
 * 获取当前 Tab 对应的订单列表
 * 将 activeTab 的值作为 status 参数传后端
 */
async function fetchOrders() {
  loading.value = true
  try {
    const res = await request.get('/order/list', { params: { status: activeTab.value } })
    orders.value = res.records || []
  } catch {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

/**
 * 确认收货
 * 弹出确认对话框 → 调用确认收货接口 → 刷新订单列表
 * @param {Object} order - 订单对象
 */
async function confirmReceive(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await request.put(`/order/${order.id}/confirm`)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err?.message || '操作失败')
  }
}

/**
 * 打开退货申请弹窗
 * @param {Object} order - 订单对象
 */
function requestRefund(order) {
  refundRef.value.open(order.id)
}

/** 页面挂载时加载订单列表 */
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
