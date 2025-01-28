<script setup lang="ts">
import { ref, computed } from 'vue'
import type {
  GetFileListByPageRequest,
  GetFileListByPageResponse
} from '@/api/admin/file/types'
import { getFileListByPage } from '@/api/admin/file'
import Utils from '@/utils/Utils'

const layout = computed(() => {
  return `total, prev, pager, next, jumper`
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
const handlePageSizeChange = (size: number) => {
  tableData.value.pageSize = size
  tableData.value.pageNum = 1
  loadDataList()
}

/**
 * 切换页码
 * @param pageNum
 */
const handlePageNoChange = (pageNum: number) => {
  tableData.value.pageNum = pageNum
  loadDataList()
}
</script>

<template>
  <div class="container">
    <!--    <div class="top">-->
    <!--      <div class="top-op">-->
    <!--        <el-button type="success" @click="showAddOrUpdateUserDialog(0,null)">-->
    <!--          <span class="iconfont icon-xinzengyonghu"></span>-->
    <!--          新增用户-->
    <!--        </el-button>-->

    <!--        <div class="iconfont icon-refresh" @click="loadDataList"></div>-->
    <!--      </div>-->
    <!--    </div>-->

    <div class="data-list">
      <el-table :data="tableData.list" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="文件名" width="200" />
        <el-table-column label="大小" width="100">
          <template #default="scope">
            {{ Utils.sizeToStr(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="identifier" label="唯一标识" width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="path" label="路径" />
        <!--        <el-table-column label="操作">-->
        <!--          <template #default="scope">-->
        <!--            <el-button size="small" @click="showAddOrUpdateUserDialog(1,scope.row)">-->
        <!--              编辑-->
        <!--            </el-button>-->
        <!--            <el-popconfirm :title="scope.row.status === 0 ? '确认启用该用户？': '确认禁用该用户？'" width="160"-->
        <!--                           @confirm="updateUserStatus(scope.row)">-->
        <!--              <template #reference>-->
        <!--                <el-button size="small" :type="(scope.row.status === 0 ? 'success':'warning')">-->
        <!--                  {{ (scope.row.status === 0 ? '启用' : '禁用') }}-->
        <!--                </el-button>-->
        <!--              </template>-->
        <!--            </el-popconfirm>-->
        <!--            &lt;!&ndash;            <el-button size="small" type="danger">&ndash;&gt;-->
        <!--            &lt;!&ndash;              删除&ndash;&gt;-->
        <!--            &lt;!&ndash;            </el-button>&ndash;&gt;-->
        <!--          </template>-->
        <!--        </el-table-column>-->
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-if="tableData.total" background :total="tableData.total"
                       :page-sizes="[15, 30, 50, 100]"
                       :page-size="tableData.pageSize" v-model:current-page="tableData.pageNum"
                       :layout="layout" @current-change="handlePageNoChange" @size-change="handlePageSizeChange"
                       style="text-align: right" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  width: 100%;
}

//.top {
//  margin-top: 20px;
//  margin-left: 20px;
//
//  .top-op {
//    display: flex;
//    align-items: center;
//
//    .icon-xinzengyonghu {
//      font-size: 20px;
//    }
//
//    .icon-refresh {
//      cursor: pointer;
//      margin-left: 20px;
//    }
//  }
//}

.data-list {
  margin: 20px;
}

.percentage-value {
  display: block;
  font-size: 10px;
}

.pagination {
  padding-top: 10px;
  padding-right: 10px;

  .el-pagination {
    justify-content: right;
  }
}
</style>