<template>
  <div>
    <el-table ref="dataTable" :data="dataSource.list || []" :height="options.tableHeight" @sort-change="sortChange"
              :stripe="options.stripe" :border="options.border" header-row-class-name="table-header-row"
              highlight-current-row :default-sort="{ prop: defaultProp, order: defaultOrder }"
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
    </el-table>
    <!-- 分页 -->
    <div class="pagination" v-if="showPagination">
      <el-pagination v-if="dataSource.total" background :total="dataSource.total"
                     :page-sizes="[15, 30, 50, 100]"
                     :page-size="dataSource.pageSize" :current-page="dataSource.pageNum" :layout="layout"
                     @current-change="pageNumChange" @size-change="pageSizeChange"
                     style="text-align: right"></el-pagination>
    </div>
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
const emit = defineEmits(['selection-change', 'row-click', 'sort-change', 'page-size-change', 'page-num-change'])

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
   * 默认排序字段
   */
  defaultProp: String,

  /**
   * 默认排序方式
   */
  defaultOrder: String
})

const layout = computed(() => {
  return `total, ${props.showPageSize ? 'sizes' : ''}, prev, pager, next, jumper`
})

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
 * 当排序条件发生变化时会触发该方法
 * @param data
 */
const sortChange = (data: any) => {
  emit('sort-change', data)
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

/**
 * 修改排序
 */
const sort = (prop: string, order: string) => {
  dataTable.value.sort(prop, order)
}

/**
 * 切换每页的大小
 * @param size
 */
const pageSizeChange = (size: number) => {
  emit('page-size-change', size)
}

/**
 * 切换页码
 * @param pageNum
 */
const pageNumChange = (pageNum: number) => {
  emit('page-num-change', pageNum)
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ setCurrentRow, clearSelection, clearSort, toggleRowSelection, sort })
</script>

<style scoped lang="scss">
.pagination {
  padding-top: 10px;
  padding-right: 10px;
}

.el-pagination {
  justify-content: right;
}

:deep(.el-table__cell) {
  padding: 4px 0;
}
</style>