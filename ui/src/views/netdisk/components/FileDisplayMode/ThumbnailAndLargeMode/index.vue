<script setup lang="ts">
import Icon from '@/components/Icon.vue'
import { computed, ref } from 'vue'
import type { PropType } from 'vue'
import Utils from '@/utils/Utils'
import type { UserFileInfo } from '@/api/v1/file/types'

export interface Config {
  width: number,
  height: number,
  iconWidth: number
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
  config: {
    type: Object as PropType<Config>,
    default: () => ({
      width: 128,
      height: 166,
      iconWidth: 60
    })
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
  fetch: Function,
  initFetch: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  sortChange: Function
})

// 顶部 60，内容区域距离顶部20，内容上下间距15*2 分页区域高度46
const topHeight = 60 + 20 + 30 + 46

const tableHeight = ref(
  props.options.tableHeight ? props.options.tableHeight : window.innerHeight - topHeight - props.options.extHeight
)

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
  dataTable.value.clearSort()
}

// 将子组件暴露出去，否则父组件无法调用
defineExpose({ setCurrentRow, clearSelection, clearSort })

// 行点击
const handleRowClick = (row: any) => {
  emit('rowClick', row)
}

const selectedArray = ref<Array<number>>([])

// 选中修改
const handleSelectionChange = (userFile: UserFileInfo) => {
  emit('rowSelected', userFile)

  if (userFile && userFile.id) {
    const index = selectedArray.value.indexOf(userFile.id)
    index !== -1 ? selectedArray.value.splice(index, 1) : selectedArray.value.push(userFile.id)
  }
  console.log('dasdasdasdasdad', selectedArray.value)
}

/**
 * 加载下一页
 */
const loadNextPage = () => {
  props.dataSource.pageNum = props.dataSource.pageNum + 1
  props.fetch()
}
</script>

<template>
  <div class="container" v-infinite-scroll="loadNextPage" :infinite-scroll-disabled="disabled"
       :style="{height: tableHeight + 'px'}">
    <div class="file-items-container">
      <div class="file-item" v-for="userFile in dataSource.list" :key="userFile.id" @click="handleRowClick(userFile)"
           :style="{width: config.width + 'px', height: config.height + 'px',
           'background-color': selectedArray.indexOf(userFile.id) !== -1 ? '#f3fafe' : ''}">
        <div class="top">
          <el-checkbox size="large" @change="handleSelectionChange(userFile)"
                       :style="{visibility: selectedArray.indexOf(userFile.id) !== -1 ? 'visible' : ''}" />
          <div class="other-button">
            <span class="iconfont icon-share" />
            <span class="iconfont icon-download" />
            <el-dropdown>
              <span class="iconfont icon-more" />
              <template #dropdown>
                <el-dropdown-menu>
                  <div class="op-dropdown-item">
                    <el-dropdown-item>
                      <span class="iconfont icon-download op-dropdown-iconfont" @click="download(row)" />
                      <span class="op-dropdown-txt">下载</span>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <span class="iconfont icon-delete op-dropdown-iconfont" @click="delFile(row)" />
                      <span class="op-dropdown-txt">删除</span>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <span class="iconfont icon-edit op-dropdown-iconfont" @click="showEditPanel(index)" />
                      <span class="op-dropdown-txt">重命名</span>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <span class="iconfont icon-move op-dropdown-iconfont" @click="moveFile(row)" />
                      <span class="op-dropdown-txt">移动</span>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <span class="iconfont icon-copy op-dropdown-iconfont" @click="copyFile(row)" />
                      <span class="op-dropdown-txt">复制</span>
                    </el-dropdown-item>
                  </div>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="content">
          <Icon :width="config.iconWidth" :fileType="userFile.itemType === 1 ? userFile.fileType : -1" />
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
    </div>
    <div v-if="loadingStatus" class="loading" v-loading="loadingStatus" element-loading-text="加载中..."></div>
    <div v-if="noMore" class="no_more">没有更多了</div>
  </div>
</template>

<style scoped lang="scss">

.container {
  display: flex;
  flex-direction: column;
  //flex-wrap: wrap;
  overflow: auto;
}

.file-items-container {
  display: flex;
  flex-wrap: wrap;
}

.file-item {
  display: flex;
  flex-direction: column;
  text-align: center;
  width: 128px;
  height: 166px;
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
    }

    .other-button {
      // 默认隐藏top部分，但占据位置
      visibility: hidden;
      display: flex;
      gap: 6px;
      border-radius: 4px;
      background-color: #ffffff;
      padding-left: 3px;
      padding-right: 2px;
      padding-top: 3px;
      padding-bottom: 1px;

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

.loading {
  height: 70px;
  margin-bottom: 20px;
}

.no_more {
  height: 50px;
  text-align: center;
  line-height: 50px;
  color: #25252B5C;
  margin-bottom: 20px;
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