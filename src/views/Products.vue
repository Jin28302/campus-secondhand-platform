<template>
  <div class="products-page">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品"
        clearable
        style="width: 260px"
        @keyup.enter="search"
      >
        <template #append>
          <el-button @click="search">搜索</el-button>
        </template>
      </el-input>

      <el-select v-model="category" placeholder="商品分类" clearable style="width: 150px" @change="search">
        <el-option label="书籍教材" value="书籍教材" />
        <el-option label="电子产品" value="电子产品" />
        <el-option label="生活好物" value="生活好物" />
        <el-option label="代步出行" value="代步出行" />
        <el-option label="学习办公" value="学习办公" />
        <el-option label="其他" value="其他" />
      </el-select>

      <el-select v-model="sort" placeholder="排序方式" style="width: 180px" @change="search">
        <el-option label="价格升序" value="price_asc" />
        <el-option label="价格降序" value="price_desc" />
        <el-option label="销量升序" value="sales_asc" />
        <el-option label="销量降序" value="sales_desc" />
        <el-option label="好评率升序" value="rating_asc" />
        <el-option label="好评率降序" value="rating_desc" />
      </el-select>
    </div>

    <div v-loading="loading" class="product-grid">
      <el-card
        v-for="item in products"
        :key="item.id"
        class="product-card"
        shadow="hover"
        @click="$router.push(`/product/${item.id}`)"
      >
        <img :src="item.image" :alt="item.name" class="product-img" />
        <div class="product-info">
          <p class="product-name">{{ item.name }}</p>
          <p class="product-price">¥{{ item.price }}</p>
        </div>
      </el-card>
    </div>

    <div v-if="!loading && products.length === 0" class="empty">
      <el-empty description="暂无商品" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchProducts"
        @size-change="fetchProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const keyword = ref(route.query.keyword || '')
const sort = ref('')
const category = ref(route.query.category || '')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const products = ref([])
const loading = ref(false)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/products', {
      params: {
        keyword: keyword.value,
        sort: sort.value,
        category: category.value,
        page: page.value,
        pageSize: pageSize.value,
      },
    })
    products.value = res.list || []
    total.value = res.total || 0
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchProducts()
}

onMounted(fetchProducts)
</script>

<style scoped>
.products-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  align-items: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.product-card {
  cursor: pointer;
}

.product-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  padding-top: 10px;
}

.product-name {
  margin: 0 0 6px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin: 0;
  color: #e6a23c;
  font-size: 16px;
  font-weight: bold;
}

.empty {
  padding: 60px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
