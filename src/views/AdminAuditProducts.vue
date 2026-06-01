<!--
  管理员-商品审核页 - 审核商家发布的商品
  主要功能：查看待审核商品列表（含缩略图）、通过/拒绝审核
  涉及接口：GET /api/admin/products/pending（待审核商品）、PUT /api/admin/product/:id/approve（通过）、
            PUT /api/admin/product/:id/reject（拒绝）
-->
<template>
  <div v-loading="loading" class="audit-products-page">
    <h2>商品审核</h2>

    <!-- 待审核商品列表 -->
    <el-table :data="products" stripe>
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img :src="row.image" class="thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="condition" label="新旧程度" width="100" />
      <el-table-column prop="merchantName" label="商家" width="100" />
      <el-table-column prop="createdAt" label="提交时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" link @click="audit(row, 'approved')">
            通过
          </el-button>
          <el-button type="danger" size="small" link @click="audit(row, 'rejected')">
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="products.length === 0 && !loading" class="empty">
      <el-empty description="暂无待审核商品" />
    </div>
  </div>
</template>

<script setup>
/**
 * 管理员-商品审核页 - 审核待审核的商品
 * 审核通过/拒绝后前端从列表中移除
 * 后端根据 URL 路径区分 approve（通过）和 reject（拒绝）
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** 页面加载状态 */
const loading = ref(false)
/** 待审核商品列表 */
const products = ref([])

/**
 * 获取待审核商品列表
 * 调用管理员接口获取 status=pending 的商品
 */
async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/admin/products/pending')
    products.value = res || []
  } catch {
    ElMessage.error('获取待审核商品失败')
  } finally {
    loading.value = false
  }
}

/**
 * 审核商品（通过或拒绝）
 * 弹出确认对话框 → 调用审核接口（URL 中拼接 approve 或 reject）
 * → 成功后从列表中移除
 * @param {Object} product - 商品对象
 * @param {string} result - 审核结果：'approved'（通过）或 'rejected'（拒绝）
 */
async function audit(product, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${action}商品「${product.name}」？`, '审核确认', {
      type: result === 'approved' ? 'success' : 'warning',
    })
    // 后端通过 URL 路径区分 approve/reject
    await request.put(`/admin/product/${product.id}/${result === 'approved' ? 'approve' : 'reject'}`)
    ElMessage.success(`已${action}`)
    // 审核后从列表中移除
    products.value = products.value.filter((p) => p.id !== product.id)
  } catch {}
}

/** 页面挂载时获取待审核商品 */
onMounted(fetchProducts)
</script>

<style scoped>
.audit-products-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.audit-products-page h2 {
  margin: 0 0 16px;
}

.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}

.empty {
  padding: 60px 0;
}
</style>
