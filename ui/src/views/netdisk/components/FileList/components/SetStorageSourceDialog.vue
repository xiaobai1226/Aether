<template>
  <el-dialog
    v-model="dialogVisible"
    title="设置存储源"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="formData" label-width="100px">
      <el-form-item label="文件夹">
        <el-input v-model="folderName" disabled />
      </el-form-item>
      <el-form-item label="选择存储源">
        <el-select
          v-model="formData.storageSourceId"
          placeholder="请选择存储源"
          style="width: 100%"
        >
          <el-option
            v-for="item in storageSourceList"
            :key="item.id"
            :label="item.name + (item.isDefault ? ' (默认)' : '')"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-alert
        title="注意：更改存储源后，该文件夹下的所有文件将被迁移到新的存储位置。"
        type="warning"
        :closable="false"
        style="margin-top: 10px"
      />
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorageSourceList } from '@/api/v1/storage-source'
import { setFolderStorageSource } from '@/api/v1/file/storage-source'
import type { StorageSourceDTO } from '@/api/v1/storage-source/types'

const dialogVisible = ref(false)
const loading = ref(false)
const folderName = ref('')
const folderId = ref<number>(0)
const storageSourceList = ref<StorageSourceDTO[]>([])

const formData = reactive({
  storageSourceId: 0
})

/**
 * 显示对话框
 */
const show = async (folder: { id: number; name: string; storageSourceId?: number }) => {
  folderId.value = folder.id
  folderName.value = folder.name
  formData.storageSourceId = folder.storageSourceId || 0
  
  // 加载存储源列表
  try {
    const response = await getStorageSourceList()
    storageSourceList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取存储源列表失败')
    return
  }
  
  dialogVisible.value = true
}

/**
 * 确认设置
 */
const handleConfirm = async () => {
  if (!formData.storageSourceId) {
    ElMessage.warning('请选择存储源')
    return
  }
  
  loading.value = true
  try {
    await setFolderStorageSource({
      folderId: folderId.value,
      storageSourceId: formData.storageSourceId
    })
    ElMessage.success('设置存储源成功')
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error.message || '设置存储源失败')
  } finally {
    loading.value = false
  }
}

const emit = defineEmits(['success'])

defineExpose({ show })
</script>

<style scoped lang="scss">
</style>

