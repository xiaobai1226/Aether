<script setup lang="ts">
import type { Ref } from 'vue'
import { computed, ref } from 'vue'
import Utils from '@/utils/Utils'
import Grid from '@/components/Grid.vue'
import type { UserFileInfo } from '@/api/v1/file/types'
import Icon from '@/components/Icon.vue'

/**
 * 父类回调方法
 */
const emit = defineEmits(['click', 'update-selected', 'download', 'del-file', 'show-edit-panel', 'move-file', 'copy-file'])

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
   * 加载
   */
  loading: {
    type: Boolean,
    default: false
  }
})

const tableOptions = ref({
  extHeight: 50,
  selectType: 'checkbox'
})

/**
 * 已选择项ID集合
 */
const selectedIds: Ref<number[]> = ref([])

/**
 * 鼠标所在的索引
 */
const mouseLocatedIndex = ref<number>(-1)

/**
 * 下拉菜单出现/消失标志
 */
const visibleChangeFlag = ref<boolean>(false)


/**
 * 下拉菜单出现/消失时触发器
 */
const visibleChange = (val: boolean, index: number) => {
  visibleChangeFlag.value = val
  mouseLocatedIndex.value = index
}

/**
 * 背景是否展示
 */
const isBackgroundShow = (index: number) => {
  return (mouseLocatedIndex.value === index) && visibleChangeFlag.value
}

/**
 * 下载
 */
const download = (userFile: UserFileInfo) => {
  emit('download', userFile)
}

/**
 * 更新已选中项
 */
const updateSelected = (selectedIds: number[]) => {
  emit('update-selected', selectedIds)
}

/**
 * 当选择项发生变化时会触发该方法
 */
const selectionChange = (newSelectedIds: number[]) => {
  updateSelected(newSelectedIds)
}

/**
 * 是否全选
 */
const isSelectAll = computed({
  get() {
    return selectedIds.value.length === props.dataSource.list.length
  },
  set(val: boolean) {
    if (val) {
      selectedIds.value = props.dataSource.list
        .filter((userFile) => userFile.id)
        .map((userFile) => userFile.id as number)
    } else {
      selectedIds.value = []
    }

    updateSelected(selectedIds.value)
  }
})

/**
 * 是否已选择，但未全部选择
 */
const isIndeterminate = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < props.dataSource.list.length)

/**
 * 清除选中
 */
const clearSelection = () => {
  selectedIds.value = []
  updateSelected(selectedIds.value)
}

/**
 * 恢复选中状态
 */
const restoreSelection = (parentSelectedIds: number[]) => {
  if (parentSelectedIds.length > 0) {
    selectedIds.value = parentSelectedIds
  }
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ clearSelection, restoreSelection })
</script>

<template>
  <div class="op">
    <el-checkbox v-model="isSelectAll" :indeterminate="isIndeterminate" />
    <span v-if="selectedIds.length === 0">全选</span>
    <span>共 {{ dataSource.total }} 项 </span>
    <span v-show="selectedIds.length > 0">已选中 {{ selectedIds.length }} 个文件/文件夹</span>
  </div>
  <el-checkbox-group class="reset-checkbox-group" v-model="selectedIds" @change="selectionChange">
    <Grid :dataSource="dataSource" :fetch="fetch" :loading="loading" :options="tableOptions">
      <template #content="{item: userFile, index}">
        <div class="file-item" @click="emit('click', userFile)"
             :style="{'background-color': selectedIds.indexOf(userFile.id) !== -1 ? '#f3fafe' : isBackgroundShow(index) ? '#f7f9fc' : ''}">
          <div class="top" @click.stop>
            <el-checkbox :value="userFile.id"
                         :style="{visibility: (selectedIds.indexOf(userFile.id) !== -1 || isBackgroundShow(index)) ? 'visible' : ''}" />
            <div class="other-button" :style="{visibility: isBackgroundShow(index) ? 'visible' : ''}">
              <!--              <span class="iconfont icon-share" />-->
              <span class="iconfont icon-download" @click="download(userFile)" />
              <el-dropdown :show-timeout="0" @visible-change="(val: boolean) => visibleChange(val,index)">
                <span class="iconfont icon-more" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <div class="op-dropdown-item">
                      <el-dropdown-item @click="download(userFile)">
                        <span class="iconfont icon-download op-dropdown-iconfont" />
                        <span class="op-dropdown-txt">下载</span>
                      </el-dropdown-item>
                      <el-dropdown-item @click="emit('del-file', userFile)">
                        <span class="iconfont icon-delete op-dropdown-iconfont" />
                        <span class="op-dropdown-txt">删除</span>
                      </el-dropdown-item>
                      <el-dropdown-item @click="emit('show-edit-panel', index)">
                        <span class="iconfont icon-edit op-dropdown-iconfont" />
                        <span class="op-dropdown-txt">重命名</span>
                      </el-dropdown-item>
                      <el-dropdown-item @click="emit('move-file', userFile)">
                        <span class="iconfont icon-move op-dropdown-iconfont" />
                        <span class="op-dropdown-txt">移动</span>
                      </el-dropdown-item>
                      <el-dropdown-item @click="emit('copy-file', userFile)">
                        <span class="iconfont icon-copy op-dropdown-iconfont" />
                        <span class="op-dropdown-txt">复制</span>
                      </el-dropdown-item>
                    </div>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="content">
            <Icon :width="60" :fileType="userFile.itemType === 1 ? userFile.fileType : -1" />
            <el-tooltip placement="bottom" effect="light" :hide-after="0">
              <div>
                <div class="name">{{ userFile.name }}</div>
                <div class="message">{{ userFile.size ? Utils.sizeToStr(userFile.size) : userFile.updateTime }}</div>
              </div>
              <template #content>
                <div style="width: 160px;">
                  <div>
                    名称：{{ userFile.name }}
                  </div>
                  <div>
                    大小：{{ userFile.size ? Utils.sizeToStr(userFile.size) : '-' }}
                  </div>
                  <div>
                    修改日期：{{ userFile.updateTime }}
                  </div>
                </div>
              </template>
            </el-tooltip>
          </div>
        </div>
      </template>
    </Grid>
  </el-checkbox-group>
</template>

<style scoped lang="scss">
.op {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #25262BB8;
  margin-bottom: 5px;

  .el-checkbox {
    display: flex;
    align-items: center;

    :deep .el-checkbox__inner {
      border-radius: 4px;
    }
  }
}

.reset-checkbox-group {
  all: initial; /* 重置所有样式 */
  display: block; /* 根据需要重新设置显示方式 */
}

.file-item {
  display: flex;
  flex-direction: column;
  text-align: center;
  width: 128px;
  height: 170px;
  margin-left: 32px;
  margin-bottom: 24px;
  border-radius: 8px;
  cursor: pointer;

  &:hover {
    background-color: #f7f9fc;

    .top {
      .el-checkbox, .other-button {
        visibility: visible;
      }
    }
  }

  .top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding-top: 8px;
    padding-left: 5px;
    padding-right: 3px;

    .el-checkbox {
      // 默认隐藏top部分，但占据位置
      visibility: hidden;
      height: auto;

      :deep .el-checkbox__inner {
        border-radius: 4px;
      }
    }

    .other-button {
      // 默认隐藏top部分，但占据位置
      visibility: hidden;
      display: flex;
      gap: 6px;
      border-radius: 4px;
      background-color: #ffffff;
      padding: 3px 2px 1px 3px;

      .icon-share, .icon-download {
        font-size: 12px;
        color: #06a7ff;
      }

      .icon-more {
        font-size: 15px;
        color: #06a7ff;
      }
    }
  }

  .content {
    margin-top: 4px;

    .name {
      font-size: 12px;
      color: #03081a;
      line-height: 18px;
      max-height: 36px;
      overflow: hidden;
      text-overflow: ellipsis;
      word-wrap: break-word;
      word-break: break-all;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-top: 8px;
      margin-left: 15px;
      margin-right: 15px;

      &:hover {
        color: #06a7ff;
      }
    }

    .message {
      font-size: 12px;
      color: #818999;
      margin-top: 3px;
    }
  }
}

.op-dropdown-item {
  .icon-download {
    font-size: 10px;
  }

  .icon-delete {
    font-size: 11px;
  }

  .icon-edit {
    font-size: 12px;
  }

  .icon-move {
    font-size: 11px;
  }

  .icon-copy {
    font-size: 12px;
  }

  .op-dropdown-iconfont {
    margin-right: 5px;
  }

  .op-dropdown-txt {
    font-size: 12px;
  }
}
</style>