<template>
  <div class="merchant-products-page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="showPublish = true">发布商品</el-button>
    </div>

    <el-table v-loading="loading" :data="products" stripe>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="condition" label="新旧程度" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'on' ? 'success' : 'info'" size="small">
            {{ row.status === 'on' ? '在售' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'on'"
            type="warning"
            size="small"
            link
            @click="offShelf(row)"
          >
            下架
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showPublish" title="发布商品" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择类别">
            <el-option label="书籍教材" value="书籍教材" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="生活好物" value="生活好物" />
            <el-option label="代步出行" value="代步出行" />
            <el-option label="学习办公" value="学习办公" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
        <el-form-item label="新旧程度" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择">
            <el-option label="全新" value="全新" />
            <el-option label="几乎全新" value="几乎全新" />
            <el-option label="轻微使用" value="轻微使用" />
            <el-option label="明显使用" value="明显使用" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <ImageUpload v-model="form.images" :multiple="true" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const products = ref([])
const showPublish = ref(false)
const publishing = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  category: '',
  price: null,
  stock: 1,
  condition: '',
  images: [],
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入类别', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  condition: [{ required: true, message: '请选择新旧程度', trigger: 'change' }],
}

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/merchant/products')
    products.value = res.list || []
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

async function offShelf(row) {
  try {
    await ElMessageBox.confirm(`确定下架「${row.name}」？`, '提示', { type: 'warning' })
    await request.put('/product/off-shelf', { productId: row.id })
    row.status = 'off'
    ElMessage.success('已下架')
  } catch {}
}

function resetForm() {
  form.name = ''
  form.category = ''
  form.price = null
  form.stock = 1
  form.condition = ''
  form.images = []
  formRef.value?.resetFields()
}

async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.images || form.images.length === 0) {
    return ElMessage.warning('请上传至少一张商品图片')
  }

  publishing.value = true
  try {
    await request.post('/product', {
      name: form.name,
      category: form.category,
      price: form.price,
      stock: form.stock,
      condition: form.condition,
      images: form.images,
    })
    ElMessage.success('发布成功')
    showPublish.value = false
    fetchProducts()
  } catch (err) {
    ElMessage.error(err?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(fetchProducts)
</script>

<style scoped>
.merchant-products-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}
</style>
