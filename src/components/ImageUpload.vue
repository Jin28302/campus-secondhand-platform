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
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: { type: [String, Array], default: null },
  multiple: { type: Boolean, default: false },
  limit: { type: Number, default: 9 },
})

const emit = defineEmits(['update:modelValue'])

const fileList = ref([])
const uploading = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    if (!val || (Array.isArray(val) && val.length === 0)) {
      fileList.value = []
    }
  }
)

async function uploadFile(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await request.post('/upload', fd)
  return res.url
}

async function handleChange(file) {
  uploading.value = true
  try {
    const url = await uploadFile(file.raw)
    if (props.multiple) {
      const urls = Array.isArray(props.modelValue) ? [...props.modelValue, url] : [url]
      emit('update:modelValue', urls)
    } else {
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
