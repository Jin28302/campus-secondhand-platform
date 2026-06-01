<!--
  主页（首页）- 应用入口页面
  主要功能：顶部搜索栏、分类快捷入口、推荐商品（已上架前8条）展示
  涉及接口：GET /api/product/search?pageNum=1&pageSize=8（推荐商品列表）
-->
<template>
  <div class="home-page">
    <!-- 搜索栏：输入关键词，回车或点击搜索按钮跳转到商品列表页 -->
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

    <!-- 分类入口：6个商品分类按钮，点击跳转到对应分类的商品列表 -->
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

    <!-- 推荐商品区域：展示已上架商品的前8条 -->
    <div class="section-title">已上架商品</div>
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
          <p class="product-meta">
            <el-tag size="small" type="info">{{ item.condition }}</el-tag>
            <span class="sales">已售 {{ item.sales || 0 }}</span>
          </p>
          <p class="product-price">{{ Number(item.price).toFixed(2) }}元</p>
        </div>
      </el-card>
    </div>

    <!-- 空状态提示：无已上架商品时显示 -->
    <div v-if="!loading && products.length === 0" class="empty">
      <el-empty description="暂无已上架商品" />
    </div>
  </div>
</template>

<script setup>
/**
 * 主页 - 应用入口页面
 * 展示搜索栏、分类快捷入口和推荐商品列表
 * 搜索和分类点击均跳转到商品列表页并携带查询参数
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Monitor, ShoppingBag, Bicycle, EditPen, MoreFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

/** Vue Router 实例 */
const router = useRouter()
/** 页面加载动画 */
const loading = ref(false)
/** 推荐商品列表 */
const products = ref([])
/** 搜索关键词 */
const keyword = ref('')

/**
 * 搜索跳转
 * 携带 keyword 查询参数跳转到商品列表页
 */
function goSearch() {
  router.push({ path: '/products', query: { keyword: keyword.value } })
}

/**
 * 商品分类定义
 * label: 分类显示名称，value: 分类值（传后端），icon: 分类图标
 */
const categories = [
  { label: '书籍教材', value: '书籍教材', icon: Reading },
  { label: '电子产品', value: '电子产品', icon: Monitor },
  { label: '生活好物', value: '生活好物', icon: ShoppingBag },
  { label: '代步出行', value: '代步出行', icon: Bicycle },
  { label: '学习办公', value: '学习办公', icon: EditPen },
  { label: '其他', value: '其他', icon: MoreFilled },
]

/**
 * 分类点击跳转
 * 携带 category 查询参数跳转到商品列表页
 * @param {string} category - 分类名称
 */
function goCategory(category) {
  router.push({ path: '/products', query: { category } })
}

/**
 * 获取推荐商品列表
 * 请求已上架商品的前 8 条作为首页推荐展示
 */
async function fetchRecommended() {
  loading.value = true
  try {
    const res = await request.get('/product/search', { params: { pageNum: 1, pageSize: 8 } })
    products.value = res.records || []
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

/** 页面挂载时获取推荐商品 */
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
  margin: 0 0 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px;
}

.product-meta .sales {
  font-size: 12px;
  color: #909399;
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
