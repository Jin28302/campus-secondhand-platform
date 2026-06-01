<!--
  图片上传组件 - 支持单图/多图上传，自动校验文件类型和大小
  主要功能：通过 v-model 双向绑定图片 URL（单图模式下为字符串，多图模式下为字符串数组）
          上传前校验文件类型必须是图片且大小不超过 5MB
          自动上传到后端并获取返回的图片 URL
  使用方式：<ImageUpload v-model="imageUrl" /> 或 <ImageUpload v-model="imageUrls" :multiple="true" />
  涉及接口：POST /api/upload（图片上传 - FormData 格式）
  Props:
    - modelValue: String|Array, v-model 绑定值（单图为 URL 字符串，多图为 URL 数组）
    - multiple: Boolean, 是否允许多图上传，默认 false
    - limit: Number, 最大上传数量，默认 9
  Events:
    - update:modelValue, 更新 v-model 值
-->
<template>
  <el-upload
    :file-list="fileList"
    :auto-upload="false"
    :multiple="multiple"
    :limit="limit"
    list-type="picture-card"
    accept="image/*"
    :on-change="handleChange"
    :on-remove="handleRemove"
  >
    <el-icon><Plus /></el-icon>
  </el-upload>
</template>

<script setup>
/**
 * 图片上传组件
 * 使用 el-upload 组件实现，auto-upload 设为 false 由组件内部手动控制上传
 * 支持单图和多图两种模式，通过 multiple prop 控制
 * 监听 modelValue 变化，当外部清空时同步清空文件列表
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

/** 组件 Props 定义 */
const props = defineProps({
  /** v-model 绑定的值：单图模式为字符串 URL，多图模式为字符串数组 */
  modelValue: { type: [String, Array], default: null },
  /** 是否允许多图上传 */
  multiple: { type: Boolean, default: false },
  /** 最大上传文件数量 */
  limit: { type: Number, default: 9 },
})

/** 触发 v-model 更新事件 */
const emit = defineEmits(['update:modelValue'])

/** el-upload 的文件列表（用于显示缩略图） */
const fileList = ref([])
/** 上传中标记 */
const uploading = ref(false)

/**
 * 监听外部清空操作
 * 当 modelValue 变为空（null/空字符串/空数组）时，同步清空文件列表
 */
watch(
  () => props.modelValue,
  (val) => {
    if (!val || (Array.isArray(val) && val.length === 0)) {
      fileList.value = []
    }
  }
)

/**
 * 上传单个文件到后端
 * 使用 FormData 格式上传，返回图片 URL
 * @param {File} file - 浏览器原生 File 对象
 * @returns {Promise<string>} 上传后的图片 URL
 */
async function uploadFile(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await request.post('/upload', fd)
  return typeof res === 'string' ? res : res.url
}

/**
 * 处理文件选中事件
 * 校验文件类型是否为图片，大小是否不超过 5MB
 * 校验通过后上传到后端并更新 modelValue
 * @param {Object} file - el-upload 的 onChange 参数
 */
async function handleChange(file) {
  // 校验文件类型必须是 image/*
  const isImage = file.raw.type.startsWith('image/')
  const isUnder5M = file.raw.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    fileList.value = fileList.value.filter((f) => f.uid !== file.uid)
    return
  }
  // 校验文件大小不超过 5MB
  if (!isUnder5M) {
    ElMessage.error('图片大小不能超过 5MB')
    fileList.value = fileList.value.filter((f) => f.uid !== file.uid)
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(file.raw)
    if (props.multiple) {
      // 多图模式：追加 URL 到数组
      const urls = Array.isArray(props.modelValue) ? [...props.modelValue, url] : [url]
      emit('update:modelValue', urls)
    } else {
      // 单图模式：替换为当前图片 URL
      fileList.value = [file]
      emit('update:modelValue', url)
    }
  } catch {
    ElMessage.error('图片上传失败')
    fileList.value = fileList.value.filter((f) => f.uid !== file.uid)
  } finally {
    uploading.value = false
  }
}

/**
 * 处理文件删除事件
 * 从 modelValue 中移除对应索引的 URL
 * 多图模式按文件列表索引匹配删除
 * @param {Object} file - el-upload 的 onRemove 参数
 */
function handleRemove(file) {
  if (props.multiple) {
    const urls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    const idx = fileList.value.findIndex((f) => f.uid === file.uid)
    if (idx !== -1) urls.splice(idx, 1)
    emit('update:modelValue', urls)
  } else {
    emit('update:modelValue', '')
  }
}
</script>
