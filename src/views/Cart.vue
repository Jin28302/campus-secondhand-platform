<!--
  购物车页 - 查看和管理购物车中的商品
  主要功能：按商家分组展示、修改数量、单选/全选、删除商品、结算跳转
  涉及接口：GET /api/cart/grouped（购物车分组列表）、PUT /api/cart/:id?quantity=（修改数量）、DELETE /api/cart/:id（删除）
-->
<template>
  <div v-loading="loading" class="cart-page">
    <h2>购物车</h2>

    <!-- 空购物车状态 -->
    <div v-if="merchantGroups.length === 0 && !loading" class="empty">
      <el-empty description="购物车为空">
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else>
      <!-- 全选控制行 -->
      <div class="select-all">
        <el-checkbox v-model="allSelected" @change="toggleAll">全选</el-checkbox>
      </div>

      <!-- 按商家分组展示购物车商品 -->
      <div v-for="group in merchantGroups" :key="group.merchantId" class="merchant-group">
        <!-- 商家名称 + 全选复选框 -->
        <div class="merchant-header">
          <el-checkbox
            :model-value="isGroupAllSelected(group)"
            @change="(val) => toggleGroup(group, val)"
          />
          <span class="merchant-name">{{ group.merchantName }}</span>
        </div>

        <!-- 该商家下的商品列表 -->
        <div v-for="item in group.items" :key="item.id" class="cart-item" :class="{ 'sold-out': item.stock === 0 }">
          <!-- 库存为0时禁用选择 -->
          <el-checkbox v-model="item.selected" :disabled="item.stock === 0" @change="updateAllSelected" />
          <img :src="item.image" :alt="item.name" class="item-img" />
          <div class="item-info">
            <p class="item-name">{{ item.name }}</p>
            <p class="item-price">¥{{ Number(item.price).toFixed(2) }}</p>
          </div>
          <!-- 库存为0时显示售罄标签，否则显示数量修改器 -->
          <el-tag v-if="item.stock === 0" type="danger" size="small">已售罄</el-tag>
          <el-input-number
            v-else
            v-model="item.quantity"
            :min="1"
            :max="item.stock"
            @change="(val, prev) => handleQuantityChange(item, val, prev)"
          />
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <el-button type="danger" link @click="handleRemove(item)">删除</el-button>
        </div>
      </div>

      <!-- 底部结算栏：显示已选商品数量和合计金额 -->
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
/**
 * 购物车页 - 管理用户购物车
 * 从后端获取按商家分组的购物车数据，转换为前端使用的格式
 * 支持修改数量、单选/全选商品、删除、结算跳转
 * 价格兼容 camelCase 和下划线两种字段名（适配 MyBatis-Plus 返回格式）
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** Vue Router 实例 */
const router = useRouter()
/** 页面加载状态 */
const loading = ref(false)
/** 按商家分组的购物车数据 */
const merchantGroups = ref([])
/** 全选复选框状态 */
const allSelected = ref(false)

/**
 * 获取购物车数据并按商家分组
 * 后端返回 CartGroupVO 列表，包含商家信息和该商家下的商品列表
 * 前端将后端字段映射为统一格式，兼容 camelCase 和下划线两种字段名
 */
async function fetchCart() {
  loading.value = true
  try {
    const res = await request.get('/cart/grouped')
    // 后端返回分组数据，适配为前端使用的格式
    merchantGroups.value = (res || []).map((group) => ({
      ...group,
      items: group.items.map((item) => {
        const p = item.product || {}
        // 兼容后端 MyBatis-Plus 的 camelCase 和下划线两种字段名
        const discount = p.discountPrice ?? p.discount_price
        const original = p.originalPrice ?? p.original_price
        const images = p.images || ''
        return {
          ...item,
          id: item.cartId,            // 购物车项ID
          name: p.name,               // 商品名称
          price: Number(discount > 0 ? discount : original) || 0, // 价格：优先折扣价
          stock: p.stock || 0,        // 库存
          image: typeof images === 'string' ? images.split(',')[0]?.trim() || '' : images[0] || '',  // 首张图片
          selected: false,            // 默认未选中
        }
      }),
    }))
    // 检查是否有购物车数量超过当前库存的，自动调整并提示
    let overflowMsg = ''
    merchantGroups.value.forEach(g => {
      g.items.forEach(i => {
        if (i.stock === 0) {
          i.quantity = 1
          overflowMsg = overflowMsg || '部分商品已售罄，无法购买'
        } else if (i.quantity > i.stock) {
          overflowMsg = overflowMsg || '部分商品库存不足，数量已自动调整'
          i.quantity = i.stock
        }
      })
    })
    if (overflowMsg) ElMessage.warning(overflowMsg)
  } catch {
    ElMessage.error('获取购物车失败')
  } finally {
    loading.value = false
  }
}

/** 所有商品展平列表（跨所有商家分组） */
const allItems = computed(() => merchantGroups.value.flatMap((g) => g.items))

/** 已选商品列表 */
const selectedItems = computed(() => allItems.value.filter((i) => i.selected))

/** 已选商品总数量 */
const selectedCount = computed(() => selectedItems.value.reduce((sum, i) => sum + i.quantity, 0))

/** 已选商品总金额（保留两位小数） */
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

/**
 * 判断某商家分组的商品是否全部选中
 * @param {Object} group - 商家分组对象
 * @returns {boolean}
 */
/** 有库存的商品 */
const inStockItems = computed(() => allItems.value.filter((i) => i.stock > 0))

function isGroupAllSelected(group) {
  const available = group.items.filter((i) => i.stock > 0)
  return available.length > 0 && available.every((i) => i.selected)
}

/** 切换商家分组全选 - 仅对有库存的商品生效 */
function toggleGroup(group, val) {
  group.items.forEach((i) => { if (i.stock > 0) i.selected = val })
  updateAllSelected()
}

/** 全局全选 - 仅对有库存的商品生效 */
function toggleAll(val) {
  inStockItems.value.forEach((i) => (i.selected = val))
}

/** 更新全选复选框的选中状态（根据所有项是否都选中） */
function updateAllSelected() {
  allSelected.value = allItems.value.length > 0 && allItems.value.every((i) => i.selected)
}

/**
 * 修改商品数量
 * 向后端同步数量变更，失败则回滚到旧值
 * @param {Object} item - 购物车项
 * @param {number} val - 新数量
 * @param {number} prev - 旧数量（用于失败回滚）
 */
async function handleQuantityChange(item, val, prev) {
  try {
    await request.put(`/cart/${item.id}`, null, { params: { quantity: val } })
  } catch {
    // 接口失败时恢复数量到旧值
    item.quantity = prev
    ElMessage.error('更新数量失败')
  }
}

/**
 * 删除购物车商品
 * 弹出确认对话框 → 调用删除接口 → 前端移除该项（含空分组清理）
 * @param {Object} item - 要删除的购物车项
 */
async function handleRemove(item) {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
    await request.delete(`/cart/${item.id}`)
    // 从本地分组中移除
    const group = merchantGroups.value.find((g) =>
      g.items.some((i) => i.id === item.id)
    )
    if (group) {
      group.items = group.items.filter((i) => i.id !== item.id)
      // 如果该商家下无商品则移除整个分组
      if (group.items.length === 0) {
        merchantGroups.value = merchantGroups.value.filter((g) => g !== group)
      }
    }
    updateAllSelected()
    ElMessage.success('已删除')
  } catch {}
}

/**
 * 结算跳转
 * 将已选中的购物车项 ID 列表通过 URL query 传递给结算页
 */
function checkout() {
  const ids = selectedItems.value.map((i) => i.id)
  router.push({ path: '/checkout', query: { ids: ids.join(',') } })
}

/** 页面挂载时加载购物车数据 */
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

/* 售罄商品样式：降低透明度 */
.cart-item.sold-out {
  opacity: 0.5;
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
