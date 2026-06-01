<!--
  管理员-商家等级设置页 - 管理商家等级
  主要功能：查看商家列表、用星级评分组件设置商家等级（Lv.1 - Lv.5）
  涉及接口：GET /api/seller/list（商家列表）、PUT /api/admin/user/:id（设置等级字段 sellerLevel）
-->
<template>
  <div v-loading="loading" class="merchant-level-page">
    <h2>商家等级设置</h2>

    <!-- 商家列表：展示名称、联系方式、当前等级、设置等级 -->
    <el-table :data="merchants" stripe>
      <el-table-column prop="name" label="商家名称" width="140" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="level" label="当前等级" width="100">
        <template #default="{ row }">
          <el-tag type="warning" size="small">Lv.{{ row.level || 1 }}</el-tag>
        </template>
      </el-table-column>
      <!-- 用 1-5 星级评分组件设置等级（el-rate max=5） -->
      <el-table-column label="设置等级" width="200">
        <template #default="{ row }">
          <el-rate v-model="row.newLevel" :max="5" @change="(val) => setLevel(row, val)" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="merchants.length === 0 && !loading" class="empty">
      <el-empty description="暂无商家" />
    </div>
  </div>
</template>

<script setup>
/**
 * 管理员-商家等级设置页
 * 使用 Element Plus 的 el-rate（星级评分）组件实现 1-5 级设置
 * 设置失败时恢复显示原等级
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

/** 页面加载状态 */
const loading = ref(false)
/** 商家列表（含 newLevel 字段用于 el-rate 绑定） */
const merchants = ref([])

/**
 * 获取商家列表
 * 为每个商家添加 newLevel 字段用于 el-rate 组件双向绑定
 */
async function fetchMerchants() {
  loading.value = true
  try {
    const res = await request.get('/seller/list')
    merchants.value = (res.records || []).map((m) => ({ ...m, newLevel: m.level || 1 }))
  } catch {
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 设置商家等级
 * 调用管理员更新用户接口（传入 sellerLevel 字段）
 * 失败时恢复 el-rate 显示
 * @param {Object} merchant - 商家对象
 * @param {number} level - 新的等级（1-5）
 */
async function setLevel(merchant, level) {
  try {
    await request.put(`/admin/user/${merchant.id}`, { sellerLevel: level })
    merchant.level = level
    ElMessage.success(`已设置为 Lv.${level}`)
  } catch (err) {
    // 失败时恢复显示
    merchant.newLevel = merchant.level || 1
    ElMessage.error(err?.message || '设置失败')
  }
}

/** 页面挂载时加载商家列表 */
onMounted(fetchMerchants)
</script>

<style scoped>
.merchant-level-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.merchant-level-page h2 {
  margin: 0 0 16px;
}

.empty {
  padding: 60px 0;
}
</style>
