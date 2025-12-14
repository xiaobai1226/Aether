<template>
  <div class="storage-source-management">
    <div class="header">
      <h2>存储源管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加存储源
      </el-button>
    </div>

    <!-- 首次使用引导 -->
    <el-alert
      v-if="storageSourceList.length === 0 && !loading"
      title="欢迎使用！"
      type="info"
      description="您还没有配置任何存储源。存储源用于指定文件的存储位置，请先添加至少一个存储源才能使用网盘功能。"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    />

    <!-- 存储源列表 -->
    <el-table
      v-loading="loading"
      :data="storageSourceList"
      style="width: 100%"
      :empty-text="loading ? '加载中...' : '暂无数据'"
    >
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          {{ getTypeName(row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="path" label="存储路径" min-width="250" show-overflow-tooltip />
      <el-table-column prop="isDefault" label="默认" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.isDefault !== 1"
            link
            type="primary"
            size="small"
            @click="handleSetDefault(row)"
          >
            设为默认
          </el-button>
          <el-button
            v-if="row.isDefault !== 1"
            link
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加存储源弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加存储源" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入存储源名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="addForm.type" placeholder="请选择存储源类型" style="width: 100%">
            <el-option label="本地存储" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储路径" prop="path">
          <el-input v-model="addForm.path" placeholder="请输入绝对路径，如：/data/aether" />
          <div class="form-tip">路径必须是绝对路径，且具有读写权限</div>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑存储源弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑存储源" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入存储源名称" />
        </el-form-item>
        <el-form-item label="存储路径">
          <el-input v-model="editForm.path" disabled />
          <div class="form-tip">路径不可修改</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getStorageSourceList,
  addStorageSource,
  updateStorageSource,
  deleteStorageSource,
  setDefaultStorageSource
} from '@/api/v1/storage-source'
import type {
  StorageSourceDTO,
  AddStorageSourceRequest,
  UpdateStorageSourceRequest,
  StorageSourceType
} from '@/api/v1/storage-source/types'
import { useAccountStore } from '@/stores/account'

const accountStore = useAccountStore()

const loading = ref(false)
const submitting = ref(false)
const storageSourceList = ref<StorageSourceDTO[]>([])

const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const addFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

const addForm = ref<AddStorageSourceRequest>({
  name: '',
  type: 0 as StorageSourceType,
  path: '',
  isDefault: 0
})

const editForm = ref<UpdateStorageSourceRequest & { path?: string }>({
  id: 0,
  name: '',
  path: ''
})

const addRules: FormRules = {
  name: [{ required: true, message: '请输入存储源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择存储源类型', trigger: 'change' }],
  path: [
    { required: true, message: '请输入存储路径', trigger: 'blur' },
    {
      pattern: /^([a-zA-Z]:[\\/]|\/)/,
      message: '路径必须是绝对路径',
      trigger: 'blur'
    }
  ]
}

const editRules: FormRules = {
  name: [{ required: true, message: '请输入存储源名称', trigger: 'blur' }]
}

// 获取类型名称
function getTypeName(type: number) {
  return type === 0 ? '本地存储' : '未知'
}

// 加载存储源列表
async function loadStorageSourceList() {
  loading.value = true
  try {
    const res = await getStorageSourceList()
    storageSourceList.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || '加载存储源列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加弹窗
function showAddDialog() {
  addForm.value = {
    name: '',
    type: 0,
    path: '',
    isDefault: storageSourceList.value.length === 0 ? 1 : 0
  }
  addDialogVisible.value = true
}

// 添加存储源
async function handleAdd() {
  if (!addFormRef.value) return

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await addStorageSource({
          ...addForm.value,
          isDefault: addForm.value.isDefault ? 1 : 0
        })
        ElMessage.success('添加成功')
        addDialogVisible.value = false
        await loadStorageSourceList()
        
        // 更新账户信息中的hasStorageSource状态
        accountStore.accountInfo.hasStorageSource = true
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 编辑
function handleEdit(row: StorageSourceDTO) {
  editForm.value = {
    id: row.id,
    name: row.name,
    path: row.path
  }
  editDialogVisible.value = true
}

// 更新存储源
async function handleUpdate() {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await updateStorageSource({
          id: editForm.value.id,
          name: editForm.value.name
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        await loadStorageSourceList()
      } catch (error: any) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 设为默认
async function handleSetDefault(row: StorageSourceDTO) {
  try {
    await setDefaultStorageSource(row.id)
    ElMessage.success('设置成功')
    await loadStorageSourceList()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
  }
}

// 删除
async function handleDelete(row: StorageSourceDTO) {
  try {
    await ElMessageBox.confirm('确定要删除该存储源吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteStorageSource(row.id)
    ElMessage.success('删除成功')
    await loadStorageSourceList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadStorageSourceList()
})
</script>

<style scoped lang="scss">
.storage-source-management {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }
}
</style>

