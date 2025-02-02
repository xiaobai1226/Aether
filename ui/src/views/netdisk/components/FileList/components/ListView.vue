<script setup lang="ts">
import { ref } from 'vue'
import Utils from '@/utils/Utils'
import Icon from '@/components/Icon.vue'
import type { Column } from '@/components/Table.vue'
import type { UserFileInfo } from '@/api/v1/file/types'

/**
 * 父类回调方法
 */
const emit = defineEmits(['click', 'update-selected', 'download', 'del-file', 'show-edit-panel', 'move-file', 'copy-file'])

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
   * 排序改变
   */
  sortChange: Function,

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

const tableOptions = ref({
  extHeight: 50,
  selectType: 'checkbox'
})

/**
 * 显示操作栏的索引 -1 为不展示，其他为要展示行的索引
 */
const showActionBarIndex = ref<number>(-1)
/**
 * 展示操作栏
 */
const showActionBar = (index: number) => {
  showActionBarIndex.value = index
}
/**
 * 隐藏操作栏
 */
const hideActionBar = () => {
  showActionBarIndex.value = -1
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
 * 清除排序
 */
const clearSort = () => {
  dataTableRef.value.clearSort()
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ clearSelection, clearSort, restoreSelection })
</script>

<template>
  <div class="total_number">
    <span>共 {{ dataSource.total }} 项 </span>
    <span v-show="selectedIds.length > 0">已选中 {{ selectedIds.length }} 个文件/文件夹</span>
  </div>
  <Table ref="dataTableRef" :columns="columns"
         :dataSource="dataSource" :fetch="fetch" :initFetch="false" :options="tableOptions"
         :loading="loading" :sortChange="sortChange" @selection-change="selectionChange">
    <!-- 文件名称 -->
    <template #fileName="{index, row}">
      <div class="file-item" @mouseenter="showActionBar(index)" @mouseleave="hideActionBar">
        <!-- 只有图片或视频，并且已经是转码成功状态才展示图片-->
        <!--              <template v-if="row.category == 1 || row.category == 3">-->
        <!--                <Icon :thumbnail="row.thumbnail" :width="32"></Icon>-->
        <!--              </template>-->
        <!--              <template v-else>-->
        <!-- 如果是文件-->
        <Icon v-if="row.itemType == 1" :fileType=row.fileType></Icon>
        <!-- 如果是文件夹-->
        <Icon v-if="row.itemType == 0" :fileType="-1"></Icon>
        <!--              </template>-->
        <span class="file-name" :title="row.name">
              <span @click="emit('click', row)">{{ row.name }}</span>
          <!-- TODO 需要删除 -->
          <!--              <span v-if="row.status == 0" class="transfer-status">转码中</span>-->
          <!--              <span v-if="row.status == 1" class="transfer-status transfer-fail">转码失败</span>-->
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
  <!--                <span class="iconfont icon-share" @click="shareFile(row)">分享</span>-->
                  <span class="iconfont icon-download" @click="emit('download', row)">下载</span>
                  <span class="iconfont icon-delete" @click="emit('del-file', row)">删除</span>
                  <span class="iconfont icon-edit" @click="emit('show-edit-panel', index)">重命名</span>
                  <span class="iconfont icon-move" @click="emit('move-file', row)">移动</span>
                  <span class="iconfont icon-copy" @click="emit('copy-file', row)">复制</span>
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
</style>