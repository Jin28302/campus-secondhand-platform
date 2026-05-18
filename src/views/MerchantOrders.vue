<template>
  <div v-loading="loading" class="merchant-orders-page">
    <h2>订单管理</h2>

    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div v-for="item in row.items" :key="item.id" class="order-product">
            {{ item.name }} × {{ item.quantity }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'refund_pending'"
            type="warning"
            size="small"
            link
            @click="openAudit(row)"
          >
            审核退货
          </el-button>
          <el-button
            v-if="row.status === 'completed' && !row.merchantReviewed"
            type="success"
            size="small"
            link
            @click="openReview(row)"
          >
            评价买家
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <ReviewDialog ref="reviewRef" type="merchant" @success="fetchOrders" />

    <el-dialog v-model="auditVisible" title="审核退货" width="440px">
      <div class="audit-info">
        <p><strong>订单号：</strong>{{ currentOrder?.orderNo }}</p>
        <p><strong>退货原因：</strong>{{ currentOrder?.refundReason }}</p>
      </div>

      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditResult">
            <el-radio value="approved">同意退货</el-radio>
            <el-radio value="rejected">拒绝退货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditResult === 'rejected'" label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ReviewDialog from '@/components/ReviewDialog.vue'

const loading = ref(false)
const orders = ref([])
const auditVisible = ref(false)
const auditing = ref(false)
const currentOrder = ref(null)
const auditResult = ref('approved')
const rejectReason = ref('')
const reviewRef = ref()

function openReview(row) {
  reviewRef.value.open(row.id)
}

function statusLabel(status) {
  const map = {
    pending: '待收货',
    completed: '已完成',
    refund_pending: '退货待审核',
    refunded: '已退货',
    refund_rejected: '退货被拒',
  }
  return map[status] || status
}

function statusTagType(status) {
  const map = {
    pending: 'warning',
    completed: 'success',
    refund_pending: 'danger',
    refunded: 'info',
    refund_rejected: '',
  }
  return map[status] || ''
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await request.get('/merchant/orders')
    orders.value = res.list || []
  } catch {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

function openAudit(order) {
  currentOrder.value = order
  auditResult.value = 'approved'
  rejectReason.value = ''
  auditVisible.value = true
}

async function submitAudit() {
  if (auditResult.value === 'rejected' && !rejectReason.value.trim()) {
    return ElMessage.warning('请输入拒绝原因')
  }

  auditing.value = true
  try {
    await request.put('/refund/audit', {
      orderId: currentOrder.value.id,
      result: auditResult.value,
      reason: auditResult.value === 'rejected' ? rejectReason.value : undefined,
    })
    ElMessage.success('审核完成')
    auditVisible.value = false
    fetchOrders()
  } catch (err) {
    ElMessage.error(err?.message || '审核失败')
  } finally {
    auditing.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.merchant-orders-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.merchant-orders-page h2 {
  margin: 0 0 16px;
}

.order-product {
  font-size: 13px;
  line-height: 1.6;
}

.audit-info p {
  margin: 6px 0;
}
</style>
