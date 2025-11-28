<script setup lang="ts">
import { ElMessage } from 'element-plus'
import UploadPopup from '@/components/UploadPopup.vue'
import { ResultErrorMsgEnum } from '@/enums/ResultErrorMsgEnum'
import { type PropType, ref, onUnmounted } from 'vue'

/**
 * 父类回调方法
 */
const emit = defineEmits(['reload', 'handle-delete', 'show-edit-panel', 'handle-download', 'update-move-copy-ids', 'show-folder-dialog'])

const reload = () => {
  emit('reload')
}

const updateMoveCopyIds = (newCurrentMoveOrCopyFileIds: number[]) => {
  emit('update-move-copy-ids', newCurrentMoveOrCopyFileIds)
}

const showFolderDialog = (type: number) => {
  emit('show-folder-dialog', type)
}

const props = defineProps({
  /**
   * 已选择的ID
   */
  selectedIds: {
    type: Array<number>,
    default: []
  },

  /**
   * 加载
   */
  loading: {
    type: Boolean,
    default: false
  },

  /**
   * 当前目录路径
   */
  currentPath: {
    type: [String, null] as PropType<string | null>,
    default: null
  },

  /**
   * 分类ID
   */
  currentCategory: {
    type: [Number, null] as PropType<number | null>,
    default: null
  }
})

/**
 * 延迟刷新的定时器
 */
let reloadTimer: ReturnType<typeof setTimeout> | null = null

/**
 * 刷新防抖延迟时间（毫秒）
 * 用于避免短时间内（特别是秒传场景）频繁刷新页面
 */
const RELOAD_DEBOUNCE_DELAY = 500

/**
 * 上传完成后重新加载
 * 
 * 核心逻辑：
 * 1. 只刷新上传路径匹配当前页面路径的文件
 * 2. 使用防抖机制，避免短时间内（特别是秒传）频繁刷新
 * 3. 延迟后再检查loading状态，确保不在刷新过程中重复刷新
 * 4. 确保最后一个上传完成的文件会触发刷新
 */
const uploadFinishReload = (uploadPath?: string) => {
  // 标准化路径：undefined 和空字符串都视为 null（根目录）
  const normalizedUploadPath = uploadPath ? uploadPath : null
  const normalizedCurrentPath = props.currentPath ? props.currentPath : null

  // 如果上传路径和当前路径不一致，不刷新
  if (normalizedUploadPath !== normalizedCurrentPath) {
    return
  }

  // 清除之前的延迟刷新定时器（防抖）
  if (reloadTimer !== null) {
    clearTimeout(reloadTimer)
  }

  // 延迟后刷新，避免秒传等场景下短时间内频繁刷新
  reloadTimer = setTimeout(() => {
    // 触发时再检查loading状态，避免在刷新过程中再次触发刷新
    if (!props.loading) {
      reload()
    }
    reloadTimer = null
  }, RELOAD_DEBOUNCE_DELAY)
}

/**
 * 组件销毁时清理定时器，防止内存泄漏
 */
onUnmounted(() => {
  if (reloadTimer !== null) {
    clearTimeout(reloadTimer)
    reloadTimer = null
  }
})

/**
 * 批量移动文件
 */
const moveFileBatch = () => {
  if (props.selectedIds.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_MOVE_CONTENT_EMPTY)
    return
  }

  updateMoveCopyIds(props.selectedIds)
  showFolderDialog(0)
}

/**
 * 批量复制文件
 */
const copyFileBatch = () => {
  if (props.selectedIds.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_COPY_CONTENT_EMPTY)
    return
  }

  updateMoveCopyIds(props.selectedIds)
  showFolderDialog(1)
}

/**
 * 批量删除选中的文件
 */
const delFileBatch = () => {
  if (props.selectedIds.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DEL_CONTENT_EMPTY)
    return
  }
  let currentDelFileIds: Array<number> = []
  currentDelFileIds = currentDelFileIds.concat(props.selectedIds)

  const message = '你确定要删除所选的文件吗？删除的文件可在10天内通过回收站还原'

  emit('handle-delete', currentDelFileIds, message)
}

/**
 * 批量下载选中的文件
 * @param type 下载类型 1 批量下载 2 打包下载
 */
const downloadBatch = (type: number) => {
  if (props.selectedIds.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DOWNLOAD_CONTENT_EMPTY)
    return
  }
  let currentDownloadFileIds: Array<number> = []
  currentDownloadFileIds = currentDownloadFileIds.concat(props.selectedIds)

  emit('handle-download', currentDownloadFileIds, type)
}

/**
 * 批量分享文件
 */
// const shareFileBatch = () => {
//   if (props.selectedIds.length == 0) {
//     ElMessage.warning(ResultErrorMsgEnum.ERROR_SHARE_CONTENT_EMPTY)
//     return
//   }
//   let currentShareFileIds: Array<number> = []
//   currentShareFileIds = currentShareFileIds.concat(props.selectedIds)
//
//   let title = ''
//   tableData.value.list.forEach((item) => {
//     if (props.selectedIds[0] == item.id) {
//       title = item.name + '等'
//     }
//   })
//
//   openShareDialog(currentShareFileIds, title)
// }

const uploadPopupRef = ref()

const openUploadPopup = () => {
  uploadPopupRef.value.show()
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ openUploadPopup })
</script>

<template>
  <div class="top-op">
    <div v-show="selectedIds.length == 0">
      <el-button type="primary" @click="openUploadPopup">
        <span class="iconfont icon-upload"></span>
        上传
      </el-button>
      <UploadPopup ref="uploadPopupRef" :category="currentCategory" :path="currentPath"
                   :callbackFunction="uploadFinishReload" />
    </div>
    <el-button type="success" v-show="selectedIds.length == 0" @click="emit('show-edit-panel',-1)">
      <span class="iconfont icon-folder-add"></span>
      新建文件夹
    </el-button>

    <el-dropdown placement="bottom" v-show="selectedIds.length > 0" :show-timeout="0">
      <el-button type="success">
        <span class="iconfont icon-download"></span>
        下载
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item>
            <span @click="downloadBatch(1)">批量下载</span>
          </el-dropdown-item>
          <el-dropdown-item>
            <span @click="downloadBatch(2)">打包下载</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-button type="danger" v-show="selectedIds.length > 0" @click="delFileBatch">
      <span class="iconfont icon-delete"></span>
      删除
    </el-button>
    <el-button type="warning" v-show="selectedIds.length > 0" @click="moveFileBatch">
      <span class="iconfont icon-move"></span>
      移动
    </el-button>
    <el-button type="warning" v-show="selectedIds.length > 0" @click="copyFileBatch">
      <span class="iconfont icon-copy"></span>
      复制
    </el-button>
    <el-tooltip content="刷新">
      <span class="iconfont icon-refresh" @click="reload" />
    </el-tooltip>
  </div>
</template>

<style scoped lang="scss">
.top-op {
  display: flex;
  align-items: center;
  gap: 12px;

  .icon-refresh {
    cursor: pointer;
  }

  .el-button + .el-button {
    margin-left: 0;
  }
}
</style>