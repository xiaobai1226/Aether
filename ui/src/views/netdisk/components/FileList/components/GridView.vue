<script setup lang="ts">
import type { Ref } from 'vue'
import { computed, ref, watch } from 'vue'
import Utils from '@/utils/Utils'
import Grid from '@/components/Grid.vue'
import type { UserFileInfo } from '@/api/v1/file/types'
import Icon, { type IconConfig } from '@/components/Icon.vue'

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
  },

  /**
   * 已选择的ID
   */
  selectedIds: {
    type: Array<number>,
    default: []
  },

  /**
   * 宽度
   */
  width: {
    type: Number,
    default: 0
  },

  /**
   * 高度
   */
  height: {
    type: Number,
    default: 0
  },

  /**
   * 显示模式 0 缩略 1 大图
   */
  mode: {
    type: Number,
    default: 0
  }
})

/**
 * 缩略模式Icon配置
 */
const thumbnailIconConfig: IconConfig = {
  width: 60,
  borderRadius: 8,
  fit: 'cover',
  imgWidth: '100%',
  imgHeight: '100%'
}

/**
 * 大图模式Icon配置
 */
const largeIconConfig: IconConfig = {
  width: 128,
  borderRadius: 8,
  fit: 'contain',
  imgWidth: 'auto',
  imgHeight: 'auto',
  imgMaxWidth: '100%',
  imgMaxHeight: '100%'
}

/**
 * 获取Icon宽度
 * @param thumbnail
 */
const getIconWidth = (thumbnail: string): number => {
  if (props.mode === 0) {
    if (thumbnail) {
      return 64
    } else {
      return 60
    }
  } else {
    return 128
  }
}

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
 * 已选择项ID集合
 */
const localSelectedIds: Ref<number[]> = ref([...props.selectedIds])

/**
 * 处理本地变量变化并通知父组件
 */
watch(localSelectedIds, (newValue) => {
  emit('update-selected', newValue)
})

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
 * 是否全选
 */
const isSelectAll = computed({
  get() {
    return localSelectedIds.value.length === props.dataSource.list.length
  },
  set(val: boolean) {
    if (val) {
      localSelectedIds.value = props.dataSource.list
        .filter((userFile) => userFile.id)
        .map((userFile) => userFile.id as number)
    } else {
      clearSelection()
    }
  }
})

/**
 * 是否已选择，但未全部选择
 */
const isIndeterminate = computed(() => localSelectedIds.value.length > 0 && localSelectedIds.value.length < props.dataSource.list.length)

/**
 * 清除选中
 */
const clearSelection = () => {
  localSelectedIds.value = []
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ clearSelection })
</script>

<template>
  <div class="op">
    <el-checkbox v-model="isSelectAll" :indeterminate="isIndeterminate" />
    <span v-if="localSelectedIds.length === 0">全选</span>
    <span>共 {{ dataSource.total }} 项 </span>
    <span v-show="localSelectedIds.length > 0">已选中 {{ localSelectedIds.length }} 个文件/文件夹</span>
  </div>
  <el-checkbox-group class="reset-checkbox-group" v-model="localSelectedIds">
    <Grid :dataSource="dataSource" :fetch="fetch" :loading="loading" :options="tableOptions">
      <template #content="{item: userFile, index}">
        <div class="file-item" @click="emit('click', userFile)"
             :style="{'background-color': localSelectedIds.indexOf(userFile.id) !== -1 ? '#f3fafe' : isBackgroundShow(index) ? '#f7f9fc' : '',
             'width': width + 'px', 'height': height + 'px'}">
          <div class="top" @click.stop>
            <el-checkbox :value="userFile.id"
                         :style="{visibility: (localSelectedIds.indexOf(userFile.id) !== -1 || isBackgroundShow(index)) ? 'visible' : ''}" />
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
            <Icon :itemType="userFile.itemType" :suffix="userFile.suffix" :thumbnail="userFile.thumbnail"
                  :icon-config="mode === 0 ? thumbnailIconConfig : largeIconConfig"
                  :width="getIconWidth(userFile.thumbnail)" />
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