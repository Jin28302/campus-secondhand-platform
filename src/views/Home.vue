<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品"
        clearable
        style="width: 480px"
        @keyup.enter="goSearch"
      >
        <template #append>
          <el-button @click="goSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 分类按钮 -->
    <div class="categories">
      <el-button
        v-for="cat in categories"
        :key="cat.value"
        class="cat-btn"
        @click="goCategory(cat.value)"
      >
        <el-icon><component :is="cat.icon" /></el-icon>
        {{ cat.label }}
      </el-button>
    </div>

    <!-- 推荐商品 -->
    <div class="section-title">推荐商品</div>
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
      <el-empty description="暂无推荐商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Monitor, ShoppingBag, Bicycle, EditPen, MoreFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const keyword = ref('')

function goSearch() {
  router.push({ path: '/products', query: { keyword: keyword.value } })
}

const categories = [
  { label: '书籍教材', value: '书籍教材', icon: Reading },
  { label: '电子产品', value: '电子产品', icon: Monitor },
  { label: '生活好物', value: '生活好物', icon: ShoppingBag },
  { label: '代步出行', value: '代步出行', icon: Bicycle },
  { label: '学习办公', value: '学习办公', icon: EditPen },
  { label: '其他', value: '其他', icon: MoreFilled },
]

function goCategory(category) {
  router.push({ path: '/products', query: { category } })
}

async function fetchRecommended() {
  loading.value = true
  try {
    const res = await request.get('/products', { params: { page: 1, pageSize: 8 } })
    products.value = res.list || []
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchRecommended)
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.search-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-bottom: 32px;
}

.cat-btn {
  font-size: 15px;
  padding: 10px 24px;
  height: auto;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #303133;
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
</style>
