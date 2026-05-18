<template>
  <div v-loading="loading" class="cart-page">
    <h2>购物车</h2>

    <div v-if="merchantGroups.length === 0 && !loading" class="empty">
      <el-empty description="购物车为空">
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else>
      <div class="select-all">
        <el-checkbox v-model="allSelected" @change="toggleAll">全选</el-checkbox>
      </div>

      <div v-for="group in merchantGroups" :key="group.merchantId" class="merchant-group">
        <div class="merchant-header">
          <el-checkbox
            :model-value="isGroupAllSelected(group)"
            @change="(val) => toggleGroup(group, val)"
          />
          <span class="merchant-name">{{ group.merchantName }}</span>
        </div>

        <div v-for="item in group.items" :key="item.id" class="cart-item">
          <el-checkbox v-model="item.selected" @change="updateAllSelected" />
          <img :src="item.image" :alt="item.name" class="item-img" />
          <div class="item-info">
            <p class="item-name">{{ item.name }}</p>
            <p class="item-price">¥{{ item.price }}</p>
          </div>
          <el-input-number
            v-model="item.quantity"
            :min="1"
            :max="item.stock"
            @change="(val) => handleQuantityChange(item, val)"
          />
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <el-button type="danger" link @click="handleRemove(item)">删除</el-button>
        </div>
      </div>

      <div class="cart-footer">
        <div class="total">
          已选 <span class="count">{{ selectedCount }}</span> 件，合计：
          <span class="amount">¥{{ totalAmount }}</span>
        </div>
        <el-button type="primary" size="large" :disabled="selectedCount === 0" @click="checkout">
          结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const merchantGroups = ref([])
const allSelected = ref(false)

async function fetchCart() {
  loading.value = true
  try {
    const res = await request.get('/cart/list')
    merchantGroups.value = (res.groups || []).map((group) => ({
      ...group,
      items: group.items.map((item) => ({ ...item, selected: false })),
    }))
  } catch {
    ElMessage.error('获取购物车失败')
  } finally {
    loading.value = false
  }
}

const allItems = computed(() => merchantGroups.value.flatMap((g) => g.items))

const selectedItems = computed(() => allItems.value.filter((i) => i.selected))

const selectedCount = computed(() => selectedItems.value.reduce((sum, i) => sum + i.quantity, 0))

const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

function isGroupAllSelected(group) {
  return group.items.length > 0 && group.items.every((i) => i.selected)
}

function toggleGroup(group, val) {
  group.items.forEach((i) => (i.selected = val))
  updateAllSelected()
}

function toggleAll(val) {
  allItems.value.forEach((i) => (i.selected = val))
}

function updateAllSelected() {
  allSelected.value = allItems.value.length > 0 && allItems.value.every((i) => i.selected)
}

async function handleQuantityChange(item, val) {
  try {
    await request.put('/cart/update', { id: item.id, quantity: val })
  } catch {
    ElMessage.error('更新数量失败')
  }
}

async function handleRemove(item) {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
    await request.delete('/cart/remove', { data: { id: item.id } })
    const group = merchantGroups.value.find((g) =>
      g.items.some((i) => i.id === item.id)
    )
    if (group) {
      group.items = group.items.filter((i) => i.id !== item.id)
      if (group.items.length === 0) {
        merchantGroups.value = merchantGroups.value.filter((g) => g !== group)
      }
    }
    updateAllSelected()
    ElMessage.success('已删除')
  } catch {}
}

function checkout() {
  const ids = selectedItems.value.map((i) => i.id)
  router.push({ path: '/checkout', query: { ids: ids.join(',') } })
}

onMounted(fetchCart)
</script>

<style scoped>
.cart-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

.cart-page h2 {
  margin: 0 0 20px;
}

.empty {
  padding: 60px 0;
}

.select-all {
  margin-bottom: 12px;
}

.merchant-group {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.merchant-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.merchant-name {
  font-weight: bold;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-img {
  width: 64px;
  height: 64px;
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

.item-price {
  margin: 0;
  color: #e6a23c;
}

.item-subtotal {
  width: 80px;
  text-align: right;
  font-weight: bold;
  color: #f56c6c;
}

.cart-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.total .count {
  color: #f56c6c;
  font-weight: bold;
}

.total .amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}
</style>
