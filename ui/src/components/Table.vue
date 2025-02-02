<template>
  <div>
    <el-table ref="dataTable" :data="dataSource.list || []" :height="options.tableHeight" @sort-change="sortChange"
              :stripe="options.stripe" v-el-table-infinite-scroll="loadNextPage" :infinite-scroll-disabled="disabled"
              :border="options.border" header-row-class-name="table-header-row" highlight-current-row
              @row-click="rowClick" @selection-change="selectionChange">
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
        <div v-if="loadingStatus" class="loading" v-loading="loadingStatus" element-loading-text="加载中..." />
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

/**
 * 列定义
 */
export interface Column {
  label: string;
  prop: string;
  scopedSlots?: string;
  width?: number;
  align?: string;
  fixed?: string;
  sortable?: string | boolean;
}

/**
 * 父类回调方法
 */
const emit = defineEmits(['selection-change', 'row-click'])

const props = defineProps({
  /**
   * 展示数据
   */
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

  /**
   * 是否展示分页
   */
  showPagination: {
    type: Boolean,
    default: true
  },

  /**
   * 是否显示页码
   */
  showPageSize: {
    type: Boolean,
    default: true
  },

  /**
   * 选项
   */
  options: {
    type: Object,
    default: function() {
      return {
        tableHeight: 0,
        showIndex: false
      }
    }
  },

  /**
   * 列定义
   */
  columns: Array<Column>,

  /**
   * 加载方法
   */
  fetch: Function,

  /**
   * 是否初始加载
   */
  initFetch: {
    type: Boolean,
    default: true
  },

  /**
   * 加载
   */
  loading: {
    type: Boolean,
    default: false
  },

  /**
   * 排序改变
   */
  sortChange: Function
})

// const layout = computed(() => {
//   return `total, ${props.showPageSize ? 'sizes' : ''}, prev, pager, next, jumper`
// })

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

// 设置行选中
const setCurrentRow = (rowKey, rowValue) => {
  let row = props.dataSource.list.find((item) => {
    return item[rowKey] == rowValue
  })
  dataTable.value.setCurrentRow(row)
}

/**
 * 行点击
 * @param row
 */
const rowClick = (row: any) => {
  emit('row-click', row)
}

/**
 * 已选中的选项数组
 * @param selection
 */
const selectionChange = (selection: []) => {
  emit('selection-change', selection)
}

/**
 * 加载下一页
 */
const loadNextPage = () => {
  props.dataSource.pageNum = props.dataSource.pageNum + 1
  props.fetch()
}

/**
 * 清除排序
 */
const clearSort = () => {
  dataTable.value.clearSort()
}

/**
 * 清除选中
 */
const clearSelection = () => {
  dataTable.value.clearSelection()
}

/**
 * 用于多选表格，切换某一行的选中状态
 */
const toggleRowSelection = (row: any, selected: boolean) => {
  dataTable.value.toggleRowSelection(row, selected)
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

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ setCurrentRow, clearSelection, clearSort, toggleRowSelection })
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