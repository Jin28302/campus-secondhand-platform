<!--
  商家商品管理页 - 商家维护自己的商品列表
  主要功能：商品列表展示、发布商品、编辑商品、下架商品
  涉及接口：GET /api/product/seller/list（商家商品列表）、POST /api/product（发布商品）、
            PUT /api/product/:id（更新商品）、PUT /api/product/:id/off（下架商品）
  状态管理：on=在售、off=已下架、pending=待审核
-->
<template>
  <div class="merchant-products-page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="showPublish = true">发布商品</el-button>
    </div>

    <!-- 商品列表表格 -->
    <el-table v-loading="loading" :data="products" stripe>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="condition" label="新旧程度" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <!-- 只有"在售"状态的商品才显示下架按钮 -->
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

    <!-- 发布/编辑商品弹窗（共用同一个表单，title 和提交接口不同） -->
    <el-dialog v-model="showPublish" :title="editingId ? '编辑商品' : '发布商品'" width="560px" @close="resetForm">
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
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="折扣价" prop="discountPrice">
          <el-input-number v-model="form.discountPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="尺寸">
          <el-input v-model="form.size" placeholder="可选" />
        </el-form-item>
        <el-form-item label="商品说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
        <el-form-item label="允许议价">
          <el-switch v-model="form.allowBargain" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
        <el-form-item label="新旧程度" prop="newDegree">
          <el-select v-model="form.newDegree" placeholder="请选择">
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
        <el-button type="primary" :loading="publishing" @click="handlePublish">
          {{ editingId ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 商家商品管理页 - 商家维护自有商品
 * 编辑模式下复用发布弹窗，自动填充已有商品数据
 * 商品字段兼容 camelCase 和下划线两种格式（后端 MyBatis-Plus 返回差异）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ImageUpload from '@/components/ImageUpload.vue'

/** 页面加载状态 */
const loading = ref(false)
/** 商品列表 */
const products = ref([])
/** 发布/编辑弹窗可见性 */
const showPublish = ref(false)
/** 发布/保存按钮 loading */
const publishing = ref(false)
/** 编辑模式下的商品 ID（null 表示新建模式） */
const editingId = ref(null)
/** 表单组件引用 */
const formRef = ref()

/** 发布/编辑表单数据 */
const form = reactive({
  name: '',            // 商品名称
  category: '',        // 商品分类
  originalPrice: null, // 原价
  discountPrice: null, // 折扣价
  size: '',            // 尺寸（可选）
  description: '',     // 商品描述（可选）
  allowBargain: false, // 是否允许议价
  stock: 1,            // 库存，默认1
  newDegree: '',       // 新旧程度
  images: [],          // 图片 URL 数组
})

/** 表单校验规则 */
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  discountPrice: [{ required: true, message: '请输入折扣价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  newDegree: [{ required: true, message: '请选择新旧程度', trigger: 'change' }],
}

/**
 * 商品状态中文映射
 * @param {string} s - 后端状态值
 * @returns {string} 中文状态名
 */
function statusLabel(s) {
  const map = { on: '在售', off: '已下架', pending: '待审核' }
  return map[s] || s
}

/**
 * 商品状态对应的 Tag 类型（颜色）
 * @param {string} s - 后端状态值
 * @returns {string} Tag 类型
 */
function statusType(s) {
  const map = { on: 'success', off: 'info', pending: 'warning' }
  return map[s] || ''
}

/**
 * 获取当前商家的商品列表
 * 调用商家专属的商品列表接口
 */
async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/product/seller/list')
    products.value = res || []
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 下架商品
 * 弹出确认对话框 → 调用下架接口 → 前端直接修改状态
 * @param {Object} row - 商品行数据
 */
async function offShelf(row) {
  try {
    await ElMessageBox.confirm(`确定下架「${row.name}」？`, '提示', { type: 'warning' })
    await request.put(`/product/${row.id}/off`)
    // 前端直接更新状态，无需重新请求
    row.status = 'off'
    ElMessage.success('已下架')
  } catch {}
}

/**
 * 打开编辑弹窗
 * 将选定商品的字段填充到表单中
 * @param {Object} row - 商品行数据
 */
function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.category = row.category
  // 兼容 camelCase 和下划线字段名
  form.originalPrice = row.originalPrice ?? row.price
  form.discountPrice = row.discountPrice ?? row.price
  form.size = row.size ?? ''
  form.description = row.description ?? ''
  form.allowBargain = row.allowBargain ?? false
  form.stock = row.stock
  form.newDegree = row.newDegree ?? row.condition
  form.images = Array.isArray(row.images) ? [...row.images] : []
  showPublish.value = true
}

/**
 * 重置表单和编辑状态
 * 弹窗关闭时调用
 */
function resetForm() {
  editingId.value = null
  form.name = ''
  form.category = ''
  form.originalPrice = null
  form.discountPrice = null
  form.size = ''
  form.description = ''
  form.allowBargain = false
  form.stock = 1
  form.newDegree = ''
  form.images = []
  formRef.value?.resetFields()
}

/**
 * 发布或编辑商品
 * 编辑模式调用 PUT 接口，新建模式调用 POST 接口
 * 提交前检查至少有一张商品图片
 */
async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.images || form.images.length === 0) {
    return ElMessage.warning('请上传至少一张商品图片')
  }

  publishing.value = true
  try {
    const data = {
      name: form.name,
      category: form.category,
      originalPrice: form.originalPrice,
      discountPrice: form.discountPrice,
      size: form.size,
      description: form.description,
      allowBargain: form.allowBargain,
      stock: form.stock,
      newDegree: form.newDegree,
      imageUrls: form.images,
    }
    if (editingId.value) {
      // 编辑模式：PUT 更新
      await request.put(`/product/${editingId.value}`, data)
      ElMessage.success('保存成功')
    } else {
      // 发布模式：POST 新建
      await request.post('/product', data)
      ElMessage.success('发布成功')
    }
    showPublish.value = false
    fetchProducts()
  } catch (err) {
    ElMessage.error(err?.message || (editingId.value ? '保存失败' : '发布失败'))
  } finally {
    publishing.value = false
  }
}

/** 页面挂载时加载商品列表 */
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
