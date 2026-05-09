<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { DirectLinkRecord, GetDirectLinkListByPageResponse } from '@/api/v1/file/types'
import { getDirectLinkListByPage, getDirectLinkUrl, revokeDirectLink, updateDirectLinkExpire } from '@/api/v1/file'
import { IMAGE, VIDEO } from '@/enums/IconEnum'

const router = useRouter()

const tableData = ref<GetDirectLinkListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 20,
  total: 0,
  totalPage: 0
})

const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getDirectLinkListByPage({
      pageNum: tableData.value.pageNum,
      pageSize: tableData.value.pageSize
    })
    if (data) {
      tableData.value = data
    } else {
      tableData.value.list = []
      tableData.value.total = 0
      tableData.value.totalPage = 0
    }
  } finally {
    loading.value = false
  }
}

const getDirectType = (record: DirectLinkRecord): 'file' | 'image' | 'video' => {
  const suffix = (record.suffix || '').toLowerCase()
  if (IMAGE.suffixSet.has(suffix)) {
    return 'image'
  }
  if (VIDEO.suffixSet.has(suffix)) {
    return 'video'
  }
  return 'file'
}

const copyDirectLink = async (record: DirectLinkRecord) => {
  const directLink = getDirectLinkUrl(record.token, getDirectType(record))
  try {
    await navigator.clipboard.writeText(directLink)
    ElMessage.success('直链已复制')
  } catch (e) {
    ElMessageBox.alert(directLink, '直链地址', {
      confirmButtonText: '知道了'
    })
  }
}

const updateExpire = (record: DirectLinkRecord) => {
  ElMessageBox.prompt('请输入新的有效期（天，0 表示永久）', '修改直链有效期', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    closeOnClickModal: false,
    inputValue: '7',
    inputValidator: (value) => {
      if (value == null || value.trim() === '') {
        return '请输入有效期'
      }
      const days = Number(value)
      if (Number.isNaN(days) || days < 0 || !Number.isInteger(days)) {
        return '有效期必须是大于等于 0 的整数'
      }
      return true
    }
  }).then(async ({ value }) => {
    await updateDirectLinkExpire({
      token: record.token,
      expireDays: Number(value)
    })
    ElMessage.success('有效期更新成功')
    loadData()
  })
}

const disableDirectLink = (record: DirectLinkRecord) => {
  ElMessageBox.confirm('确定要将该直链设为失效吗？', '失效直链', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await revokeDirectLink({
      token: record.token
    })
    ElMessage.success('直链已失效')
    loadData()
  })
}

const handlePageChange = (pageNum: number) => {
  tableData.value.pageNum = pageNum
  loadData()
}

const handlePageSizeChange = (pageSize: number) => {
  tableData.value.pageSize = pageSize
  tableData.value.pageNum = 1
  loadData()
}

const statusLabelMap: Record<string, string> = {
  ACTIVE: '生效中',
  EXPIRED: '已过期',
  DISABLED: '已失效'
}

const statusTypeMap: Record<string, 'success' | 'warning' | 'info'> = {
  ACTIVE: 'success',
  EXPIRED: 'warning',
  DISABLED: 'info'
}

const goToFolder = (folderPath?: string) => {
  if (!folderPath || folderPath === '-') {
    return
  }

  if (folderPath === '/') {
    router.push({
      path: '/netdisk/main'
    })
    return
  }

  router.push({
    path: '/netdisk/main',
    query: {
      path: folderPath
    }
  })
}

loadData()
</script>

<template>
  <div class="direct-link-page">
    <div class="header">
      <div class="title">直链记录</div>
      <el-button type="primary" plain @click="loadData">刷新</el-button>
    </div>

    <el-table :data="tableData.list" v-loading="loading" stripe>
      <el-table-column label="文件名" min-width="240">
        <template #default="{ row }">
          <div class="file-meta">
            <el-tooltip :content="row.fileName || '文件已删除'" placement="top" :show-after="300">
              <div class="file-name">{{ row.fileName || '文件已删除' }}</div>
            </el-tooltip>
            <div class="file-path-row">
              <el-icon class="folder-icon"><Folder /></el-icon>
              <el-tooltip :content="row.folderPath || '/'" placement="top" :show-after="300">
                <div class="file-path" :class="{ 'file-path-link': row.folderPath && row.folderPath !== '-' }"
                     @click="goToFolder(row.folderPath)">
                  所在目录：{{ row.folderPath || '/' }}
                </div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTypeMap[row.bizStatus]">{{ statusLabelMap[row.bizStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireAt" label="过期时间" width="200">
        <template #default="{ row }">
          <span>{{ row.expireAt || '永久有效' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="copyDirectLink(row)">复制链接</el-button>
          <el-button link type="primary" @click="updateExpire(row)">修改有效期</el-button>
          <el-button link type="danger" :disabled="row.bizStatus === 'DISABLED'" @click="disableDirectLink(row)">设为失效</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        :total="tableData.total"
        :page-size="tableData.pageSize"
        :current-page="tableData.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.direct-link-page {
  padding: 12px 20px 0 0;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #25262b;
    }
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .file-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .file-path-row {
      display: flex;
      align-items: center;
      gap: 4px;

      .folder-icon {
        color: #818999;
        font-size: 12px;
        margin-top: 1px;
      }
    }

    .file-name {
      font-size: 13px;
      color: #25262b;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .file-path {
      font-size: 12px;
      color: #818999;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;

      &.file-path-link {
        cursor: pointer;
        color: #5b9df8;
      }
    }
  }
}
</style>
