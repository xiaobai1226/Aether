<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import Utils from '@/utils/Utils'
import Icon from '@/components/Icon.vue'
import type { Column } from '@/components/Table.vue'
import type { UserFileInfo } from '@/api/v1/file/types'
import { CreateTime, SortingFieldEnum } from '@/enums/SortingFieldEnum'
import { ASC, DESC, SortingMethodEnum } from '@/enums/SortingMethodEnum'
import { useSystemStore } from '@/stores/system'

/**
 * 父类回调方法
 */
const emit = defineEmits(['click', 'update-selected', 'download', 'del-file', 'show-edit-panel', 'move-file', 'copy-file', 'set-storage-source', 'create-direct-link'])

/**
 * 获取系统配置
 */
const systemStore = useSystemStore()

/**
 * 获取网盘配置
 */
const netdiskConfig = systemStore.netdiskConfig

/**
 * 列表列定义
 */
const columns: Column[] = [
  {
    label: '文件名',
    prop: 'fileName',
    scopedSlots: 'fileName',
    sortable: 'custom'
  },
  {
    label: '修改时间',
    prop: 'updateTime',
    width: 200,
    sortable: 'custom'
  },
  {
    label: '文件大小',
    prop: 'fileSize',
    scopedSlots: 'fileSize',
    width: 200,
    sortable: 'custom'
  }
]

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
   * 已选择的ID
   */
  selectedIds: {
    type: Array<number>,
    default: []
  }

  /**
   * 是否展示分页
   */
  // showPagination: {
  //   type: Boolean,
  //   default: true
  // },

  /**
   * 是否显示页码
   */
  // showPageSize: {
  //   type: Boolean,
  //   default: true
  // },
})

// 顶部 60，内容区域距离顶部20，内容上下间距15*2 分页区域高度46
const topHeight = 60 + 20 + 30 + 46

const tableHeight = ref(
  window.innerHeight - topHeight - 50
)

const tableOptions = ref({
  tableHeight: tableHeight.value,
  selectType: 'checkbox'
})

/**
 * 显示操作栏的索引 -1 为不展示，其他为要展示行的索引
 */
const showActionBarIndex = ref<number>(-1)

/**
 * 隐藏操作栏的延迟定时器
 */
let hideTimer: number | null = null

/**
 * 下拉菜单是否打开
 */
const dropdownVisible = ref<boolean>(false)

/**
 * 展示操作栏
 */
const showActionBar = (index: number) => {
  // 清除可能存在的隐藏定时器
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  showActionBarIndex.value = index
}

/**
 * 隐藏操作栏
 */
const hideActionBar = () => {
  // 如果下拉菜单打开，则不隐藏
  if (dropdownVisible.value) {
    return
  }
  
  // 添加延迟隐藏，避免鼠标移动过程中闪烁
  hideTimer = window.setTimeout(() => {
    showActionBarIndex.value = -1
    hideTimer = null
  }, 200)
}

/**
 * 下拉菜单可见性变化
 */
const onDropdownVisibleChange = (visible: boolean) => {
  dropdownVisible.value = visible
  // 下拉菜单关闭时，如果鼠标不在行上，则隐藏操作栏
  if (!visible) {
    hideActionBar()
  }
}

/**
 * 更新已选中项
 */
const updateSelected = (newSelectedIds: number[]) => {
  emit('update-selected', newSelectedIds)
}

/**
 * 恢复选中状态
 */
const restoreSelection = () => {
  if (props.selectedIds.length > 0) {
    let matchedCount = 0
    for (const userFile of props.dataSource.list) {
      if (matchedCount === props.selectedIds.length) {
        // 当匹配数量达到 selectedIds 的长度时，立即跳出循环
        break
      }

      if (props.selectedIds.includes(userFile.id)) {
        dataTableRef.value.toggleRowSelection(userFile, true)
        matchedCount++
      }
    }
  }
}

/**
 * 当选择项发生变化时会触发该方法
 */
const selectionChange = (userFiles: UserFileInfo[]) => {
  if (!props.loading) {
    const newSelectedIds: number[] = userFiles
      .map((userFile) => userFile.id as number)
    updateSelected(newSelectedIds)
  }
}

/**
 * 当排序条件发生变化时会触发该方法
 * @param data
 */
const sortChange = (data: any) => {
  if (data.prop === netdiskConfig.sortingConfig.sortingField.elField && data.order === netdiskConfig.sortingConfig.sortingMethod.elStr) {
    return
  }

  for (const key in SortingFieldEnum) {
    const item = SortingFieldEnum[key]
    if (data.prop === item.elField) {
      systemStore.changeSortingField(item)
      break
    }
  }

  for (const key in SortingMethodEnum) {
    let item = SortingMethodEnum[key]
    if (data.order === item.elStr) {
      if (!data.order) {
        item = netdiskConfig.sortingConfig.sortingMethod === ASC ? DESC : ASC
      }
      systemStore.changeSortingMethod(item)
      break
    }
  }
}

/**
 * 表格数据ref
 */
const dataTableRef = ref()

/**
 * 清除选中
 */
const clearSelection = () => {
  updateSelected([])
  dataTableRef.value.clearSelection()
}

/**
 * 修改排序
 */
const sort = (prop: string, order: string) => {
  if (prop === CreateTime.elField) {
    dataTableRef.value.clearSort()
  } else {
    dataTableRef.value.sort(prop, order)
  }
}

/**
 * 处理更多菜单命令
 */
const handleCommand = (command: string, row: UserFileInfo, index: number) => {
  switch (command) {
    case 'direct-link':
      emit('create-direct-link', row)
      break
    case 'move':
      emit('move-file', row)
      break
    case 'copy':
      emit('copy-file', row)
      break
    case 'set-storage':
      emit('set-storage-source', row)
      break
  }
}

/**
 * 处理行点击事件
 * @param row 行数据
 */
const handleRowClick = (row: UserFileInfo) => {
  emit('click', row)
}

/**
 * 组件卸载时清理定时器
 */
onUnmounted(() => {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
})

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ clearSelection, restoreSelection, sort })
</script>

<template>
  <div class="total_number">
    <span>共 {{ dataSource.total }} 项 </span>
    <span v-show="selectedIds.length > 0">已选中 {{ selectedIds.length }} 个文件/文件夹</span>
  </div>
  <Table ref="dataTableRef" :columns="columns" :dataSource="dataSource" :fetch="fetch" :initFetch="false"
         :options="tableOptions" :loading="loading" :default-prop="netdiskConfig.sortingConfig.sortingField.elField"
         :default-order="netdiskConfig.sortingConfig.sortingMethod.elStr"
         @sort-change="sortChange" @selection-change="selectionChange" @row-click="handleRowClick">
    <!-- 文件名称 -->
    <template #fileName="{index, row}">
      <div class="file-item" @mouseenter="showActionBar(index)" @mouseleave="hideActionBar">
        <!-- 只有图片或视频，并且已经是转码成功状态才展示图片-->
        <!--              <template v-if="row.category == 1 || row.category == 3">-->
        <!--                <Icon :thumbnail="row.thumbnail" :width="32"></Icon>-->
        <!--              </template>-->
        <!--              <template v-else>-->
        <Icon :itemType="row.itemType" :suffix="row.suffix" :thumbnail="row.thumbnail" />
        <!--              </template>-->
        <span class="file-name" :title="row.name">
              {{ row.name }}
        </span>
        <!-- 新建文件夹或重命名输入栏 -->
        <!--              <div class="edit-panel" v-if="showEditPanelIndex == index">-->
        <!--                <el-input v-model.trim="editPanelFileName" ref="editPanelRef"-->
        <!--                          @keyup.enter="submitEditPanel(index)">-->
        <!--                </el-input>-->
        <!--                <span :class="['iconfont icon-right', editPanelFileName ? '' : 'not-allow']"-->
        <!--                      @click="submitEditPanel(index)" />-->
        <!--                <span class="iconfont icon-error" @click="hideEditPanel(index)" />-->
        <!--              </div>-->
        <!-- 操作栏 -->
        <span class="op">
          <template v-if="showActionBarIndex == index && row.id && row.fileStatus == 1">
            <span class="iconfont icon-download" @click.stop="emit('download', row)" title="下载">下载</span>
            <span class="iconfont icon-delete" @click.stop="emit('del-file', row)" title="删除">删除</span>
            <span class="iconfont icon-edit" @click.stop="emit('show-edit-panel', index)" title="重命名">重命名</span>
            <!-- 更多操作下拉菜单 -->
            <el-dropdown trigger="hover" :hide-on-click="true" :show-timeout="100" :hide-timeout="100"
                         @command="handleCommand($event, row, index)" 
                         @visible-change="onDropdownVisibleChange"
                         @click.stop>
              <span class="iconfont icon-more more-btn" title="更多操作">
                更多<i class="el-icon--right" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="direct-link">
                    <span class="iconfont icon-link"></span> 生成直链
                  </el-dropdown-item>
                  <el-dropdown-item command="move">
                    <span class="iconfont icon-move"></span> 移动
                  </el-dropdown-item>
                  <el-dropdown-item command="copy">
                    <span class="iconfont icon-copy"></span> 复制
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.itemType === 0" command="set-storage" divided>
                    <span class="iconfont icon-settings"></span> 设置存储源
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </span>
      </div>
    </template>
    <!-- 文件大小 -->
    <template #fileSize="{ index, row }">
      <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
      <span v-else>-</span>
    </template>
  </Table>
</template>

<style scoped lang="scss">
@import "@/styles/file.list.scss";

.total_number {
  font-size: 12px;
  color: #25262BB8;
  margin-bottom: 5px;
}

// 表格行样式 - 添加手型指针
:deep(.el-table__row) {
  cursor: pointer;
}

// 文件名 hover 效果
.file-item {
  .file-name {
    cursor: pointer;
    
    &:hover {
      color: #06a7ff;
    }
  }
}

// 下拉菜单项样式
:deep(.el-dropdown-menu__item) {
  .iconfont {
    margin-right: 8px;
    font-size: 12px;
  }
}
</style>