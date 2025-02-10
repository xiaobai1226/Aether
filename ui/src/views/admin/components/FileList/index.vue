<script setup lang="ts">
import { ref } from 'vue'
import type {
  GetFileListByPageRequest,
  GetFileListByPageResponse
} from '@/api/admin/file/types'
import { getFileListByPage, generateThumbnails } from '@/api/admin/file'
import Utils from '@/utils/Utils'
import Icon from '@/components/Icon.vue'
import ActionBar from '@/views/admin/components/FileList/components/ActionBar.vue'
import TableByPagination, { type Column } from '@/components/TableByPagination.vue'
import { isImageOrVideo } from '@/enums/IconEnum'

/**
 * 列表列定义
 */
const columns: Column[] = [
  {
    label: 'ID',
    prop: 'id',
    width: 60
  },
  {
    label: '文件名',
    prop: 'name',
    scopedSlots: 'name',
    width: 550
  },
  {
    label: '大小',
    prop: 'size',
    scopedSlots: 'size',
    width: 100
  },
  {
    label: '唯一标识',
    prop: 'identifier',
    width: 200
  },
  {
    label: '创建时间',
    prop: 'createTime',
    width: 180
  },
  {
    label: '路径',
    prop: 'path'
  },
  {
    label: '操作',
    prop: 'op',
    scopedSlots: 'op',
    width: 300
  }
]

// 顶部 60，内容区域距离顶部20，内容上下间距15*2 分页区域高度46
const topHeight = 56 + 20 + 30 + 46

const tableHeight = ref(
  window.innerHeight - topHeight - 50
)

const tableOptions = ref({
  tableHeight: tableHeight.value,
  border: true,
  stripe: true
})

/**
 * 初始化列表数据
 */
const tableData = ref<GetFileListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 30,
  total: 0,
  totalPage: 0
})

/**
 * 加载文件列表
 */
const loadDataList = () => {
  const getFileListByPageRequest: GetFileListByPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize
  }

  // 请求后台获取文件列表
  getFileListByPage(getFileListByPageRequest).then(({ data }) => {
    if (data == null) {
      tableData.value = {
        list: [],
        pageNum: 1,
        pageSize: 30,
        total: 0,
        totalPage: 0
      }
    } else {
      tableData.value.totalPage = data.totalPage
      tableData.value.total = data.total
      tableData.value.list = data.list
    }
  }).catch(() => {
  })
}

loadDataList()

/**
 * 切换每页的大小
 * @param size
 */
const pageSizeChange = (size: number) => {
  tableData.value.pageSize = size
  tableData.value.pageNum = 1
  loadDataList()
}

/**
 * 切换页码
 * @param pageNum
 */
const pageNumChange = (pageNum: number) => {
  tableData.value.pageNum = pageNum
  loadDataList()
}

/**
 * 生成缩略图
 */
const handleGenerateThumbnails = (id: number) => {
  const ids = [id].join(',')
// 请求后台获取文件列表
  generateThumbnails(ids).then(() => {
    // 重新加载数据
    loadDataList()
  })
}
</script>

<template>
  <div class="container">
    <div class="top">
      <ActionBar @generate-thumbnails="handleGenerateThumbnails" />
    </div>
    <div class="data-list">
      <TableByPagination :columns="columns" :dataSource="tableData" :initFetch="false" :options="tableOptions"
                         @page-num-change="pageNumChange" @page-size-change="pageSizeChange">
        <!-- 文件名称 -->
        <template #name="{row}">
          <div class="file-item">
            <Icon :suffix="row.suffix" :thumbnail="row.thumbnail" />
            <span class="file-name" :title="row.name">
              <span>{{ row.name }}</span>
            </span>
          </div>
        </template>
        <!-- 文件大小 -->
        <template #size="{ row }">
          <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
          <span v-else>-</span>
        </template>
        <!-- 操作栏 -->
        <template #op="{row}">
          <el-button v-if="isImageOrVideo(row.suffix)" size="small" @click="handleGenerateThumbnails(row.id)">
            生成缩略图
          </el-button>
        </template>
      </TableByPagination>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  // 让容器占据剩余宽度
  flex-grow: 1;
  height: calc(100vh - 56px);
}

.top {
  padding-top: 20px;
  padding-left: 20px;
  padding-right: 20px;

}

.data-list {
  margin: 20px;
}
</style>