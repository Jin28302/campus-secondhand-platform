<!--
  商品列表页 - 浏览和搜索所有已上架商品
  主要功能：关键词搜索、分类筛选、6种排序方式、分页展示
  涉及接口：GET /api/product/search（商品搜索 - 支持keyword/category/sort/pageNum/pageSize）
-->
<template>
  <div class="products-page">
    <!-- 工具栏：搜索框、分类下拉、排序下拉 -->
    <div class="toolbar">
      <!-- 关键词搜索 -->
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

      <!-- 分类筛选下拉 -->
      <el-select v-model="category" placeholder="商品分类" clearable style="width: 150px" @change="search">
        <el-option label="书籍教材" value="书籍教材" />
        <el-option label="电子产品" value="电子产品" />
        <el-option label="生活好物" value="生活好物" />
        <el-option label="代步出行" value="代步出行" />
        <el-option label="学习办公" value="学习办公" />
        <el-option label="其他" value="其他" />
      </el-select>

      <!-- 排序方式下拉 -->
      <el-select v-model="sort" placeholder="排序方式" style="width: 180px" @change="search">
        <el-option label="价格升序" value="price_asc" />
        <el-option label="价格降序" value="price_desc" />
        <el-option label="销量升序" value="sales_asc" />
        <el-option label="销量降序" value="sales_desc" />
        <el-option label="好评率升序" value="rating_asc" />
        <el-option label="好评率降序" value="rating_desc" />
      </el-select>
    </div>

    <!-- 商品网格列表 -->
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
          <p class="product-price">￥{{ Number(item.price).toFixed(2) }}</p>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && products.length === 0" class="empty">
      <el-empty description="暂无商品" />
    </div>

    <!-- 分页组件 -->
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
/**
 * 商品列表页 - 浏览和搜索已上架商品
 * 支持关键词搜索、分类筛选、多种排序和分页
 * 从路由 query 中读取初始搜索参数（来自主页跳转）
 * 每次搜索/筛选/排序变更时自动重置到第1页
 */
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

/** 当前路由对象，用于读取 URL 查询参数 */
const route = useRoute()
/** 搜索关键词（初始值从路由 query 读取） */
const keyword = ref(route.query.keyword || '')
/** 排序方式 */
const sort = ref('')
/** 分类筛选（初始值从路由 query 读取） */
const category = ref(route.query.category || '')
/** 当前页码 */
const page = ref(1)
/** 每页显示数量 */
const pageSize = ref(12)
/** 商品总条数 */
const total = ref(0)
/** 商品列表数据 */
const products = ref([])
/** 加载状态 */
const loading = ref(false)

/**
 * 获取商品列表
 * 携带搜索关键词、分类、排序、分页参数请求后端
 */
async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/product/search', {
      params: {
        keyword: keyword.value,
        sort: sort.value,
        category: category.value,
        pageNum: page.value,
        pageSize: pageSize.value,
      },
    })
    products.value = res.records || []
    total.value = res.total || 0
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 搜索/筛选/排序时调用
 * 将页码重置为第1页再请求数据
 */
function search() {
  page.value = 1
  fetchProducts()
}

/** 页面挂载时加载商品列表 */
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
