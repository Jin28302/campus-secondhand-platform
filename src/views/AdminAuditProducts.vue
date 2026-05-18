<template>
  <div v-loading="loading" class="audit-products-page">
    <h2>商品审核</h2>

    <el-table :data="products" stripe>
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img :src="row.image" class="thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
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

    <div v-if="products.length === 0 && !loading" class="empty">
      <el-empty description="暂无待审核商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const products = ref([])

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/admin/pending-products')
    products.value = res.list || []
  } catch {
    ElMessage.error('获取待审核商品失败')
  } finally {
    loading.value = false
  }
}

async function audit(product, result) {
  const action = result === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${action}商品「${product.name}」？`, '审核确认', {
      type: result === 'approved' ? 'success' : 'warning',
    })
    await request.put('/admin/audit-product', { productId: product.id, result })
    ElMessage.success(`已${action}`)
    products.value = products.value.filter((p) => p.id !== product.id)
  } catch {}
}

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
