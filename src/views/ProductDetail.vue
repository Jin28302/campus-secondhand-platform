<template>
  <div v-loading="loading" class="detail-page">
    <el-card v-if="product" class="detail-card">
      <div class="detail-layout">
        <div class="gallery">
          <el-carousel height="360px" indicator-position="outside">
            <el-carousel-item v-for="(img, idx) in product.images" :key="idx">
              <img :src="img" :alt="product.name" class="carousel-img" />
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p v-if="product.description" class="product-desc">{{ product.description }}</p>
          <p class="product-price">¥{{ product.price }}</p>

          <div class="meta">
            <el-tag>库存: {{ product.stock }}</el-tag>
            <el-tag type="info">{{ product.condition }}</el-tag>
          </div>

          <el-divider />

          <div class="merchant">
            <h4>商家信息</h4>
            <p>{{ product.merchant?.name }}</p>
            <p>{{ product.merchant?.phone }}</p>
          </div>

          <el-divider />

          <div class="actions">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            <el-button type="primary" :loading="adding" @click="addToCart">
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="product" class="reviews-card">
      <template #header>
        <h3>商品评价</h3>
      </template>

      <div v-if="reviews.length === 0" class="no-reviews">暂无评价</div>

      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <span class="review-user">{{ review.username }}</span>
          <el-rate :model-value="review.rating" disabled />
          <span class="review-date">{{ review.createdAt }}</span>
        </div>
        <p class="review-content">{{ review.content }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getProductRatings } from '@/api/review'

const route = useRoute()
const productId = route.params.id

const loading = ref(false)
const product = ref(null)
const reviews = ref([])
const quantity = ref(1)
const adding = ref(false)

async function fetchProduct() {
  loading.value = true
  try {
    const [res, ratings] = await Promise.all([
      request.get(`/product/${productId}`),
      getProductRatings(productId),
    ])
    product.value = res.product
    reviews.value = ratings
  } catch {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  adding.value = true
  try {
    await request.post('/cart/add', { productId, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (err) {
    ElMessage.error(err?.message || '加入购物车失败')
  } finally {
    adding.value = false
  }
}

onMounted(fetchProduct)
</script>

<style scoped>
.detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.detail-layout {
  display: flex;
  gap: 32px;
}

.gallery {
  width: 400px;
  flex-shrink: 0;
}

.carousel-img {
  width: 100%;
  height: 360px;
  object-fit: cover;
}

.info {
  flex: 1;
}

.product-name {
  margin: 0 0 12px;
  font-size: 22px;
}

.product-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px;
  white-space: pre-wrap;
}

.product-price {
  font-size: 24px;
  color: #e6a23c;
  font-weight: bold;
  margin: 0 0 16px;
}

.meta {
  display: flex;
  gap: 8px;
}

.merchant h4 {
  margin: 0 0 6px;
}

.merchant p {
  margin: 4px 0;
  color: #606266;
}

.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.reviews-card {
  margin-top: 20px;
}

.reviews-card h3 {
  margin: 0;
}

.no-reviews {
  color: #909399;
  text-align: center;
  padding: 20px;
}

.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.review-user {
  font-weight: bold;
}

.review-date {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  margin: 0;
  color: #303133;
}
</style>
