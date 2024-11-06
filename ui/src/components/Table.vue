<template>
  <div>
    <el-table ref="dataTable" :data="dataSource.list || []" :height="tableHeight" @sort-change="sortChange"
              :stripe="options.stripe" v-el-table-infinite-scroll="loadNextPage" :infinite-scroll-disabled="disabled"
              :border="options.border" header-row-class-name="table-header-row" highlight-current-row
              @row-click="handleRowClick" @selection-change="handleSelectionChange">
      <!-- selection选择框 -->
      <el-table-column v-if="options.selectType && options.selectType == 'checkbox'" type="selection" width="50"
                       align="center" />
      <!-- 序号 -->
      <el-table-column v-if="options.showIndex" label="序号" type="index" width="60" align="center" />
      <!-- 数据列 -->
      <template v-for="(column, index) in columns">
        <template v-if="column && column.scopedSlots">
          <el-table-column :key="index" :label="column.label" :prop="column.prop" :width="column.width"
                           :align="column.align || 'left'" :sortable="column.sortable">
            <template #default="scope">
              <slot :name="column.scopedSlots" :row="scope.row" :index="scope.$index"></slot>
            </template>
          </el-table-column>
        </template>
        <template v-else>
          <el-table-column :key="index" :label="column.label" :prop="column.prop" :width="column.width"
                           :align="column.align || 'left'" :fixed="column.fixed" :sortable="column.sortable">
          </el-table-column>
        </template>
      </template>
      <template v-slot:append>
        <div v-if="loadingStatus" class="loading" v-loading="loadingStatus" element-loading-text="加载中..."></div>
        <div v-if="noMore" class="no_more">没有更多了</div>
      </template>
    </el-table>
    <!-- 分页 -->
    <!--    <div class="pagination" v-if="showPagination">-->
    <!--      <el-pagination v-if="dataSource.total" background :total="dataSource.total"-->
    <!--                     :page-sizes="[15, 30, 50, 100]"-->
    <!--                     :page-size="dataSource.pageSize" v-model:current-page="dataSource.pageNum" :layout="layout"-->
    <!--                     @current-change="handlePageNoChange" @size-change="handlePageSizeChange"-->
    <!--                     style="text-align: right"></el-pagination>-->
    <!--    </div>-->
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { default as vElTableInfiniteScroll } from 'el-table-infinite-scroll'

export interface Column {
  label: string;
  prop: string;
  scopedSlots?: string;
  width?: number;
  align?: string;
  fixed?: string;
  sortable?: string | boolean;
}

const emit = defineEmits(['rowSelected', 'rowClick'])
const props = defineProps({
  dataSource: {
    type: Object,
    default: () => {
      return {
        list: [],
        pageNum: 1,
        pageSize: 15,
        total: 0
      }
    }
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  showPageSize: {
    type: Boolean,
    default: true
  },
  options: {
    type: Object,
    default: function() {
      return {
        extHeight: 0,
        showIndex: false
      }
    }
  },
  columns: Array<Column>,
  fetch: Function,
  initFetch: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  }
  // sortChange: Function
})

// const layout = computed(() => {
//   return `total, ${props.showPageSize ? 'sizes' : ''}, prev, pager, next, jumper`
// })

// 顶部 60，内容区域距离顶部20，内容上下间距15*2 分页区域高度46
const topHeight = 60 + 20 + 30 + 46

const tableHeight = ref(
  props.options.tableHeight ? props.options.tableHeight : window.innerHeight - topHeight - props.options.extHeight
)

// 初始化
// const init = () => {
//   if (props.initFetch && props.fetch) {
//     console.log("faasdasdasdasdasdadad")
//     props.fetch()
//   }
// }
//
// init()

const dataTable = ref()

/**
 * 是否没有更多
 */
const noMore = computed(() => props.dataSource.list.length >= props.dataSource.total)

/**
 * 是否禁用
 */
const disabled = computed(() => props.loading || noMore.value)

/**
 * loading状态
 */
const loadingStatus = computed(() => props.loading && props.dataSource.pageNum !== 1)

// 清除选中
const clearSelection = () => {
  dataTable.value.clearSelection()
}

// 设置行选中
const setCurrentRow = (rowKey, rowValue) => {
  let row = props.dataSource.list.find((item) => {
    return item[rowKey] == rowValue
  })
  dataTable.value.setCurrentRow(row)
}

const clearSort = () => {
  // dataTable.value.clearSort()
}

// 将子组件暴露出去，否则父组件无法调用
defineExpose({ setCurrentRow, clearSelection, clearSort })

// 行点击
const handleRowClick = (row: any) => {
  emit('rowClick', row)
}

// 多选
const handleSelectionChange = (row: any) => {
  emit('rowSelected', row)
}

/**
 * 加载下一页
 */
const loadNextPage = () => {
  props.dataSource.pageNum = props.dataSource.pageNum + 1
  props.fetch()
}

/**
 * 排序查询
 */
const sortChange = (sortMessage: any) => {
  let sortField = undefined
  if (sortMessage.prop === 'fileName') {
    sortField = 1
  } else if (sortMessage.prop === 'updateTime') {
    sortField = 2
  } else if (sortMessage.prop === 'fileSize') {
    sortField = 3
  }

  let sortOrder = undefined
  if (sortMessage.order === 'ascending') {
    sortOrder = 1
  } else if (sortMessage.order === 'descending') {
    sortOrder = 2
  }

  props.dataSource.pageNum = 1
  props.fetch(sortField, sortOrder)
}

// 切换每页的大小
// const handlePageSizeChange = (size: number) => {
//   props.dataSource.pageSize = size
//   props.dataSource.pageNum = 1
//   props.fetch()
// }

// 切换页码
// const handlePageNoChange = (pageNum: number) => {
//   props.dataSource.pageNum = pageNum
//   props.fetch()
// }
</script>

<style scoped lang="scss">
//.pagination {
//  padding-top: 10px;
//  padding-right: 10px;
//}
//
//.el-pagination {
//  justify-content: right;
//}

.loading {
  height: 70px;
  margin-bottom: 20px;
}

.no_more {
  height: 50px;
  text-align: center;
  line-height: 50px;
  color: #25252B5C
}

:deep(.el-table__cell) {
  padding: 4px 0;
}
</style>