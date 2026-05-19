<template>
  <div v-loading="loading" class="checkout-page">
    <h2>确认订单</h2>

    <div v-for="group in merchantGroups" :key="group.merchantId" class="merchant-section">
      <el-card class="order-items">
        <template #header>
          <span>{{ group.merchantName }}</span>
        </template>
        <div v-for="item in group.items" :key="item.id" class="order-item">
          <img :src="item.image" :alt="item.name" class="item-img" />
          <div class="item-info">
            <p class="item-name">{{ item.name }}</p>
            <p class="item-meta">¥{{ item.price }} × {{ item.quantity }}</p>
          </div>
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </el-card>
    </div>

    <el-card class="settle-card">
      <div class="settle-row">
        <span>商品总额</span>
        <span class="amount">¥{{ totalAmount }}</span>
      </div>

      <div class="settle-row">
        <div class="points-toggle">
          <span>积分抵扣</span>
          <el-switch v-model="usePoints" :disabled="availablePoints <= 0" />
        </div>
        <span v-if="usePoints" class="discount">-¥{{ pointsDiscount }}</span>
        <span v-else class="discount">-¥0.00</span>
      </div>

      <div class="points-hint">
        可用积分：{{ availablePoints }}（可抵扣 ¥{{ pointsDiscount }}）
      </div>

      <el-divider />

      <div class="settle-row total-row">
        <span>应付金额</span>
        <span class="final-amount">¥{{ finalAmount }}</span>
      </div>

      <div class="submit-row">
        <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">
          确认订单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const items = ref([])
const usePoints = ref(false)
const availablePoints = ref(0)

const ids = computed(() => (route.query.ids || '').split(',').filter(Boolean))
const merchantGroups = computed(() => {
  const map = {}
  items.value.forEach(item => {
    const mid = item.merchantId
    if (!map[mid]) map[mid] = { merchantId: mid, merchantName: item.merchantName, items: [] }
    map[mid].items.push(item)
  })
  return Object.values(map)
})

const totalAmount = computed(() =>
  items.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

const pointsDiscount = computed(() => (availablePoints.value / 100).toFixed(2))

const finalAmount = computed(() => {
  const total = parseFloat(totalAmount.value)
  const discount = usePoints.value ? parseFloat(pointsDiscount.value) : 0
  return Math.max(total - discount, 0).toFixed(2)
})

async function fetchCheckoutData() {
  loading.value = true
  try {
    const res = await request.get('/checkout', { params: { ids: ids.value.join(',') } })
    items.value = res.items || []
    availablePoints.value = res.availablePoints || 0
  } catch {
    ElMessage.error('获取订单信息失败')
  } finally {
    loading.value = false
  }
}

async function submitOrder() {
  submitting.value = true
  try {
    await Promise.all(
      merchantGroups.value.map(group =>
        request.post('/order/create', {
          cartItemIds: group.items.map(i => i.id),
          usePoints: usePoints.value,
          pointsAmount: usePoints.value ? availablePoints.value : 0,
        })
      )
    )
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch (err) {
    ElMessage.error(err?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

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
</style>
