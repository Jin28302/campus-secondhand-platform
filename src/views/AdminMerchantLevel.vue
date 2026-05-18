<template>
  <div v-loading="loading" class="merchant-level-page">
    <h2>商家等级设置</h2>

    <el-table :data="merchants" stripe>
      <el-table-column prop="name" label="商家名称" width="140" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="level" label="当前等级" width="100">
        <template #default="{ row }">
          <el-tag type="warning" size="small">Lv.{{ row.level || 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="设置等级" width="200">
        <template #default="{ row }">
          <el-rate v-model="row.newLevel" :max="5" @change="(val) => setLevel(row, val)" />
        </template>
      </el-table-column>
    </el-table>

    <div v-if="merchants.length === 0 && !loading" class="empty">
      <el-empty description="暂无商家" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const merchants = ref([])

async function fetchMerchants() {
  loading.value = true
  try {
    const res = await request.get('/admin/merchants')
    merchants.value = (res.list || []).map((m) => ({ ...m, newLevel: m.level || 1 }))
  } catch {
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

async function setLevel(merchant, level) {
  try {
    await request.put('/admin/merchant-level', { merchantId: merchant.id, level })
    merchant.level = level
    ElMessage.success(`已设置为 Lv.${level}`)
  } catch (err) {
    merchant.newLevel = merchant.level || 1
    ElMessage.error(err?.message || '设置失败')
  }
}

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
