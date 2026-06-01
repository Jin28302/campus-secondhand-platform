<!--
  结算页 - 确认订单并提交
  主要功能：订单预览（按商家分组）、积分抵扣开关、费用计算、提交订单
  涉及接口：POST /api/order/preview（订单预览）、POST /api/order/create（创建订单）
  积分规则：100 积分 = 1 元
-->
<template>
  <div v-loading="loading" class="checkout-page">
    <h2>确认订单</h2>

    <!-- 按商家分组展示订单商品 -->
    <div v-for="group in merchantGroups" :key="group.merchantId" class="merchant-section">
      <el-card class="order-items">
        <template #header>
          <span>{{ group.merchantName }}</span>
        </template>
        <div v-for="item in group.items" :key="item.id" class="order-item">
          <img :src="item.image" :alt="item.name" class="item-img" />
          <div class="item-info">
            <p class="item-name">{{ item.name }}</p>
            <p class="item-meta">￥{{ Number(item.price).toFixed(2) }} × {{ item.quantity }}</p>
          </div>
          <span class="item-subtotal">￥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </el-card>
    </div>

    <!-- 费用结算卡片 -->
    <el-card class="settle-card">
      <!-- 商品总额 -->
      <div class="settle-row">
        <span>商品总额</span>
        <span class="amount">￥{{ totalAmount }}</span>
      </div>

      <!-- 积分抵扣开关 -->
      <div class="settle-row">
        <div class="points-toggle">
          <span>积分抵扣</span>
          <el-switch v-model="usePoints" :disabled="availablePoints <= 0" />
        </div>
        <span v-if="usePoints" class="discount">-￥{{ pointsDiscount }}</span>
        <span v-else class="discount">-￥0.00</span>
      </div>

      <!-- 积分提示信息 -->
      <div class="points-hint">
        可用积分：{{ availablePoints }}（可抵扣 ￥{{ pointsDiscount }}）
      </div>

      <el-divider />

      <!-- 应付金额：商品总额 - 积分抵扣 -->
      <div class="settle-row total-row">
        <span>应付金额</span>
        <span class="final-amount">￥{{ finalAmount }}</span>
      </div>

      <!-- 钱包余额提示 -->
      <div class="settle-row wallet-row">
        <span>钱包余额</span>
        <span :class="walletBalance < parseFloat(finalAmount) ? 'insufficient' : 'sufficient'">
          ￥{{ walletBalance.toFixed(2) }}
        </span>
      </div>
      <div v-if="walletBalance < parseFloat(finalAmount)" class="balance-warning">
        <el-alert title="余额不足，请先充值" type="error" :closable="false" show-icon />
      </div>

      <!-- 确认订单提交按钮 -->
      <div class="submit-row">
        <el-button type="primary" size="large" :loading="submitting" :disabled="walletBalance < parseFloat(finalAmount)" @click="submitOrder">
          确认订单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 结算页 - 确认订单并提交
 * 从 URL query 读取购物车 ID 列表 → 请求订单预览 → 展示费用明细 → 提交订单
 * 积分抵扣规则：100 积分 = 1 元
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

/** 当前路由对象 */
const route = useRoute()
/** Vue Router 实例 */
const router = useRouter()
/** 页面加载状态 */
const loading = ref(false)
/** 钱包余额 */
const walletBalance = ref(0)
/** 订单提交按钮加载状态 */
const submitting = ref(false)
/** 订单商品项列表（展平后的） */
const items = ref([])
/** 是否使用积分抵扣 */
const usePoints = ref(false)
/** 用户可用积分 */
const availablePoints = ref(0)

/** 从 URL query 解析购物车 ID 列表（逗号分隔） */
const ids = computed(() => (route.query.ids || '').split(',').filter(Boolean))

/**
 * 按商家 ID 分组的订单商品
 * 将 items 按 merchantId 聚合，供模板按商家分组展示
 */
const merchantGroups = computed(() => {
  const map = {}
  items.value.forEach(item => {
    const mid = item.merchantId
    if (!map[mid]) map[mid] = { merchantId: mid, merchantName: item.merchantName, items: [] }
    map[mid].items.push(item)
  })
  return Object.values(map)
})

/** 商品总金额（保留两位小数） */
const totalAmount = computed(() =>
  items.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

/** 积分可抵扣金额（100 积分 = 1 元） */
const pointsDiscount = computed(() => (availablePoints.value / 100).toFixed(2))

/**
 * 最终应付金额
 * 计算方式：商品总额 - 积分抵扣（如果开启），最低为 0
 */
const finalAmount = computed(() => {
  const total = parseFloat(totalAmount.value)
  const discount = usePoints.value ? parseFloat(pointsDiscount.value) : 0
  return Math.max(total - discount, 0).toFixed(2)
})

/**
 * 获取订单预览数据
 * 请求后端 OrderPreviewVO，包含分组信息、总金额、可用积分等
 */
async function fetchCheckoutData() {
  loading.value = true
  try {
    const [res, walletRes] = await Promise.all([
      request.post('/order/preview', { cartIds: ids.value.map(Number) }),
      request.get('/wallet'),
    ])
    // res 是 OrderPreviewVO: { groups, totalAmount, availablePoints, pointsDeduction, actualPay }
    const allItems = (res.groups || []).flatMap(g =>
      g.items.map(i => ({
        id: i.cartId,
        merchantId: g.sellerId,
        merchantName: g.shopName,
        name: i.product?.name,
        price: i.product?.discountPrice ?? i.product?.originalPrice ?? 0,
        quantity: i.quantity,
        image: typeof i.product?.images === 'string' ? i.product.images.split(',')[0]?.trim() || '' : (i.product?.images?.[0] || ''),
      }))
    )
    items.value = allItems
    availablePoints.value = res.availablePoints || 0
    walletBalance.value = Number(walletRes.walletBalance ?? walletRes.wallet ?? 0)
  } catch {
    ElMessage.error('获取订单信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 提交订单
 * 传入购物车 ID 列表和是否使用积分标志
 * 成功后跳转到我的订单页
 */
async function submitOrder() {
  submitting.value = true
  try {
    await request.post('/order/create', {
      cartIds: ids.value.map(Number),
      usePoints: usePoints.value,
    })
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch (err) {
    ElMessage.error(err?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

/** 页面挂载时获取订单预览数据 */
onMounted(fetchCheckoutData)
</script>

<style scoped>
.checkout-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px;
}

.checkout-page h2 {
  margin: 0 0 20px;
}

.merchant-section {
  margin-bottom: 16px;
}

.order-items {
  margin-bottom: 0;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.item-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.item-subtotal {
  font-weight: bold;
}

.settle-card {
  margin-bottom: 16px;
}

.settle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.points-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
}

.amount {
  font-weight: bold;
}

.discount {
  color: #67c23a;
  font-weight: bold;
}

.points-hint {
  font-size: 12px;
  color: #909399;
  padding: 4px 0;
}

.total-row {
  font-size: 18px;
}

.final-amount {
  color: #f56c6c;
  font-size: 22px;
  font-weight: bold;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.wallet-row {
  padding: 4px 0;
}

.wallet-row .sufficient { color: #67c23a; font-weight: bold; }
.wallet-row .insufficient { color: #f56c6c; font-weight: bold; }

.balance-warning { margin-top: 8px; }
</style>
