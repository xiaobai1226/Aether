<template>
  <div>
    <div class="top">
      <ActionBar ref="actionBarRef" :selectedIds="selectedIds" :loading="loading" :currentCategory="currentCategory"
                 :currentPath="currentPath"
                 @reload="reload" @handle-delete="handleDelete" @show-edit-panel="showEditPanel"
                 @handle-download="handleDownload" @update-move-copy-ids="updateMoveCopyIds"
                 @show-folder-dialog="showFolderDialog" />
      <NavigationActionBar ref="navigationActionBarRef" />
    </div>

    <div ref="loadingRef">
      <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
        <!-- 列表模式 -->
        <ListView ref="listViewRef" v-if="netdiskConfig.displayMode.id === List.id" :dataSource="tableData"
                  :fetch="loadDataList" :initFetch="false" :loading="loading"
                  :selectedIds="selectedIds"
                  @update-selected="updateSelected" @click="click" @download="download" @del-file="delFile"
                  @show-edit-panel="showEditPanel" @move-file="moveFile" @copy-file="copyFile" />
        <!-- 缩略模式 -->
        <GridView ref="thumbnailViewRef" v-else-if="netdiskConfig.displayMode.id === Thumbnail.id"
                  :width="128" :height="170" :iconWidth="60"
                  :dataSource="tableData" :fetch="loadDataList" :loading="loading" :selectedIds="selectedIds"
                  @update-selected="updateSelected" @click="click" @download="download" @del-file="delFile"
                  @show-edit-panel="showEditPanel" @move-file="moveFile" @copy-file="copyFile" />
        <!-- 大图模式 -->
        <GridView ref="largeViewRef" v-else-if="netdiskConfig.displayMode.id === Large.id"
                  :width="168" :height="245" :iconWidth="128"
                  :dataSource="tableData" :fetch="loadDataList" :loading="loading" :selectedIds="selectedIds"
                  @update-selected="updateSelected" @click="click" @download="download" @del-file="delFile"
                  @show-edit-panel="showEditPanel" @move-file="moveFile" @copy-file="copyFile" />
      </div>
      <div class="no-data" v-else>
        <div class="no-data-inner">
          <Icon iconName="no_data" :width="120" fit="fill"></Icon>
          <div class="tips">当前目录为空，上传你的第一个文件吧</div>
          <div class="op-list">
            <div class="op-item" @click="openUploadPopup">
              <Icon iconName="file" :width="60"></Icon>
              <div>上传文件</div>
            </div>
            <div class="op-item" v-if="!currentCategory" @click="showEditPanel(-1)">
              <Icon iconName="folder" :width="60"></Icon>
              <div>新建目录</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <FolderSelect ref="folderSelectRef" @folderSelect="handleMoveOrCopyCallback" />
    <!-- 预览 -->
    <Preview ref="previewRef" />
    <!-- 分享 -->
    <!--    <ShareFile ref="shareFileRef"></ShareFile>-->
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import {
  getFileListByPage,
  newFolder,
  rename,
  del,
  move,
  copy,
  createDownloadSign,
  getDownloadUrl
} from '@/api/v1/file'
import type {
  GetFileListByPageRequest,
  GetFileListByPageResponse,
  UserFileInfo,
  NewFolderRequest,
  FileRenameRequest,
  MoveRequest,
  CopyRequest, DeleteRequest
} from '@/api/v1/file/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import Icon from '@/components/Icon.vue'
import Confirm from '@/utils/Confirm'
import FolderSelect from '@/components/FolderSelect.vue'
import { useRoute } from 'vue-router'
import Preview from '@/components/preview/Preview.vue'
import { useUserStore } from '@/stores/user'
import { RegexEnum } from '@/enums/RegexEnum'
import { ResultErrorMsgEnum } from '@/enums/ResultErrorMsgEnum'
import { useSystemStore } from '@/stores/system'
import { List, Thumbnail, Large } from '@/enums/DisplayModeEnum'
import ListView from '@/views/netdisk/components/FileList/components/ListView.vue'
import GridView from '@/views/netdisk/components/FileList/components/GridView.vue'
import ActionBar from '@/views/netdisk/components/FileList/components/ActionBar.vue'
import NavigationActionBar from '@/views/netdisk/components/FileList/components/NavigationActionBar.vue'

/**
 * 从pinia获取用户数据
 */
const userStore = useUserStore()

/**
 * 获取系统配置
 */
const systemStore = useSystemStore()

/**
 * 获取网盘配置
 */
const netdiskConfig = systemStore.netdiskConfig

const route = useRoute()

/**
 * 初始化列表数据
 */
const tableData = ref<GetFileListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 50,
  total: 0,
  totalPage: 0
})

/**
 * 分类ID
 */
const currentCategory = ref<number | null>(null)

/**
 * 当前目录路径
 */
const currentPath = ref<string | null>(null)

/**
 * 加载Ref
 */
const loadingRef = ref()

/**
 * 正在加载标识
 */
const loading = ref(false)

/**
 * 加载文件列表
 */
const loadDataList = () => {
  loading.value = true

  const getFileListByPageRequest: GetFileListByPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize,
    category: currentCategory.value,
    sortingField: netdiskConfig.sortingConfig.sortingField.id,
    sortingMethod: netdiskConfig.sortingConfig.sortingMethod.id
  }

  if (currentPath.value != null) {
    getFileListByPageRequest.path = currentPath.value as string
  }

  // 请求后台获取文件列表
  getFileListByPage(getFileListByPageRequest, getFileListByPageRequest.pageNum === 1, loadingRef.value).then(({ data }) => {
    if (data == null) {
      if (tableData.value.pageNum === 1) {
        tableData.value = {
          list: [],
          pageNum: 1,
          pageSize: 50,
          total: 0,
          totalPage: 0
        }
      }
    } else {
      if (tableData.value.pageNum === 1) {
        tableData.value = data
      } else if (tableData.value.pageNum > 1) {
        tableData.value.totalPage = data.totalPage
        tableData.value.total = data.total
        tableData.value.list = tableData.value.list.concat(data.list)
      }
    }

    // 如果是列表形式，加载数据完成后，恢复点击状态
    if (netdiskConfig.displayMode.id === List.id) {
      nextTick(() => {
        listViewRef.value && listViewRef.value.restoreSelection()
        loading.value = false
      })
      return
    }

    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/**
 * 重新加载
 */
const reload = () => {
  tableData.value.pageNum = 1
  loadDataList()
}

/**
 * 展示新建文件夹或重命名输入框
 * @param index 对应行索引，-1 为新建文件夹
 */
const showEditPanel = (index: number) => {
  let message = '新建文件夹'
  let inputValue = '新建文件夹'
  let selectEndIndex = inputValue.length

  // 如果是重命名
  if (index !== -1) {
    message = '重命名'

    let currentData = tableData.value.list[index]

    if (currentData.name) {
      inputValue = currentData.name
    }

    selectEndIndex = inputValue.length

    if (currentData.itemType == 1) {
      let lastIndex = inputValue.lastIndexOf('.')
      if (lastIndex != -1) {
        selectEndIndex = lastIndex
      }
    }
  }

  ElMessageBox.prompt('', message, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    closeOnClickModal: false,
    inputValue: inputValue,
    inputValidator: (value) => {
      // 校验名称格式
      const regex = new RegExp(RegexEnum.REGEX_FILE_NAME)
      if (!value) {
        return ResultErrorMsgEnum.ERROR_FILE_NAME_EMPTY
      } else if (value.length > 255) {
        return ResultErrorMsgEnum.ERROR_FILE_NAME_LENGTH as string
      } else if (!regex.test(value)) {
        return ResultErrorMsgEnum.ERROR_FILE_NAME_FORMAT
      }

      return true
    }
  }).then(({ value }) => {
    // 如果index不为-1则为重命名，否则为新建文件夹
    if (index !== -1) {
      // 获取行数据
      const fileData: UserFileInfo = tableData.value.list[index]

      if (!fileData.id) {
        return
      }

      // 重命名
      const data: FileRenameRequest = {
        id: fileData.id,
        newName: value
      }

      rename(data).then(() => {
        // 重新加载数据
        reload()
      })
    }
    // 新建文件夹
    else {
      const data: NewFolderRequest = {
        folderName: value
      }

      if (currentPath.value != null) {
        data.path = currentPath.value as string
      }

      newFolder(data).then(() => {
        // 重新加载数据
        reload()
      })
    }
  })

  nextTick().then(() => {
    const inputElement = document.querySelector('.el-message-box__input input') as HTMLInputElement
    if (inputElement) {
      // 选择输入框中的文字
      inputElement.select()
      inputElement.setSelectionRange(0, selectEndIndex)
    }
  })

}

/**
 * 选中的项ID
 */
const selectedIds = ref<number[]>([])

/**
 * 更新selectedIds
 */
const updateSelected = (selectIds: number[]) => {
  selectedIds.value = selectIds
}

/**
 * 文件夹选择Ref
 */
const folderSelectRef = ref()

/**
 * 打开选择目录弹窗
 * @param type 0 移动 1 复制
 */
const showFolderDialog = (type: number) => {
  folderSelectRef.value.showFolderDialog(type, currentPath.value)
}

/**
 * 当前要移动、复制的文件
 */
const currentMoveOrCopyFileIds = ref<Array<number>>([])

/**
 * 更新当前要移动、复制的文件
 */
const updateMoveCopyIds = (newCurrentMoveOrCopyFileIds: number[]) => {
  currentMoveOrCopyFileIds.value = newCurrentMoveOrCopyFileIds
}

/**
 * 移动单个文件
 */
const moveFile = (userFileInfo: UserFileInfo) => {
  if (!userFileInfo || !userFileInfo.id) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_MOVE_CONTENT_EMPTY)
    return
  }
  currentMoveOrCopyFileIds.value = []
  currentMoveOrCopyFileIds.value.push(userFileInfo.id)
  folderSelectRef.value.showFolderDialog(0, currentPath.value)
}

/**
 * 移动操作
 * @param targetPath 目标路径
 */
const handleMove = (targetPath: string) => {
  // 如果没有选中的文件，则直接返回
  if (currentMoveOrCopyFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_MOVE_CONTENT_EMPTY)
    return
  }

  if (currentPath.value == targetPath) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_MOVE_IN_CURRENT_FOLDER)
    return
  }

  const data: MoveRequest = {
    sourceIds: currentMoveOrCopyFileIds.value.join(',')
  }

  if (targetPath != null) {
    data.targetPath = targetPath
  }

  move(data).then(() => {
    clearSelection()

    // 重新加载数据
    reload()

    // 关闭选择文件夹弹窗
    folderSelectRef.value.close()
  })
}

/**
 * 复制单个文件
 */
const copyFile = (userFileInfo: UserFileInfo) => {
  if (!userFileInfo || !userFileInfo.id) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_COPY_CONTENT_EMPTY)
    return
  }
  currentMoveOrCopyFileIds.value = []
  currentMoveOrCopyFileIds.value.push(userFileInfo.id)
  folderSelectRef.value.showFolderDialog(1, currentPath.value)
}

/**
 * 复制操作
 * @param targetPath 目标路径
 */
const handleCopy = (targetPath: string) => {

  // 如果没有选中的文件，则直接返回
  if (currentMoveOrCopyFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_COPY_CONTENT_EMPTY)
    return
  }

  if (currentPath.value == targetPath) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_COPY_IN_CURRENT_FOLDER)
    return
  }

  const data: CopyRequest = {
    sourceIds: currentMoveOrCopyFileIds.value.join(',')
  }

  if (targetPath != null) {
    data.targetPath = targetPath
  }

  copy(data).then(() => {
    // 重新加载数据
    // loadDataList();

    // 关闭选择文件夹弹窗
    folderSelectRef.value.close()

    clearSelection()

    // 更新用户存储空间
    userStore.handleGetUserSpaceUsage()
  })
}

/**
 * 移动或复制操作选择文件夹弹窗回调
 * @param targetPath 目标路径
 * @param type 0 移动 1 复制
 */
const handleMoveOrCopyCallback = (targetPath: string, type: number) => {
  if (type == 0) {
    handleMove(targetPath)
  } else if (type == 1) {
    handleCopy(targetPath)
  }
}

/**
 * 删除选中的文件
 */
const delFile = (userFileInfo: UserFileInfo) => {
  if (!userFileInfo || !userFileInfo.id) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DEL_CONTENT_EMPTY)
    return
  }

  const currentDelFileIds: Array<number> = []
  currentDelFileIds.push(userFileInfo.id)

  const message = `你确定要删除 ${userFileInfo.name} 吗？删除的文件可在10天内通过回收站还原`

  handleDelete(currentDelFileIds, message)
}

/**
 * 删除文件
 *
 * @param currentDelFileIds 要删除的文件ID集合
 * @param message 提示信息
 */
const handleDelete = (currentDelFileIds: Array<number>, message: string) => {
  Confirm(message, () => {
    const data: DeleteRequest = {
      ids: currentDelFileIds.join(',')
    }

    del(data).then(() => {
      clearSelection()

      reload()
    })
  })
}

const previewRef = ref()

const actionBarRef = ref()

const navigationActionBarRef = ref()

const openUploadPopup = () => {
  actionBarRef.value.openUploadPopup()
}

/**
 * 点击
 * @param userFile 用户文件信息
 */
const click = (userFile: UserFileInfo) => {
  // 目录
  if (userFile.itemType == 0) {
    clearSelection()
    navigationActionBarRef.value.openFolder(userFile.name)
    return
  }

  previewRef.value.showPreview(userFile, 0)
}

// 下载文件
const download = (userFileInfo: UserFileInfo) => {
  if (!userFileInfo || !userFileInfo.id) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DOWNLOAD_CONTENT_EMPTY)
    return
  }

  const currentDownloadFileIds: Array<number> = []
  currentDownloadFileIds.push(userFileInfo.id)
  handleDownload(currentDownloadFileIds, 1)
}

/**
 * 下载文件
 *
 * @param currentDownloadFileIds 要下载的文件ID集合
 * @param type 下载类型 1 批量下载 2 打包下载
 */
const handleDownload = (currentDownloadFileIds: Array<number>, type: number) => {
  if (type === 1) {
    currentDownloadFileIds.forEach((id => {
      createDownloadSign(id.toString()).then(({ data }) => {
        window.open(getDownloadUrl(data))
      })
    }))
  } else if (type === 2) {
    createDownloadSign(currentDownloadFileIds.join(',')).then(({ data }) => {
      window.open(getDownloadUrl(data))
    })
  }
}

const listViewRef = ref()

const thumbnailViewRef = ref()

const largeViewRef = ref()

/**
 * 清除选中
 */
const clearSelection = () => {
  if (netdiskConfig.displayMode.id === List.id) {
    listViewRef.value && listViewRef.value.clearSelection()
  } else if (netdiskConfig.displayMode.id === Thumbnail.id) {
    thumbnailViewRef.value && thumbnailViewRef.value.clearSelection()
  } else if (netdiskConfig.displayMode.id === Large.id) {
    largeViewRef.value && largeViewRef.value.clearSelection()
  }
}

/**
 * 监听路由中category，path参数的变化
 */
watch(
  () => route.query, (newQuery, oldQuery) => {
    if (route.path !== '/netdisk/main') {
      return
    }

    const category = newQuery.category
    const path = newQuery.path

    if (Array.isArray(category)) {
      currentCategory.value = Number(category[0])
    } else if (category) {
      currentCategory.value = Number(category)
    } else {
      currentCategory.value = null
    }

    if (Array.isArray(path)) {
      currentPath.value = path[0]
    } else if (path) {
      currentPath.value = path as string
    } else {
      currentPath.value = null
    }

    nextTick().then(() => {
      navigationActionBarRef.value && navigationActionBarRef.value.updateFolderList(currentPath.value)
    })

    clearSelection()

    // 加载数据
    reload()
  }, // 可选，如果设置为 true，组件挂载时会立即触发一次回调
  { immediate: true }
)

/**
 * 监听显示模式的变化
 */
watch(() => netdiskConfig.displayMode, (newValue, oldValue) => {
  nextTick(() => {
    if (newValue.id === List.id) {
      listViewRef.value && listViewRef.value.restoreSelection()
    }
  })
})

/**
 * 监听排序配置的组合变化
 */
watch(
  () => [netdiskConfig.sortingConfig.sortingField, netdiskConfig.sortingConfig.sortingMethod], (newValues, oldValues) => {
    if (netdiskConfig.displayMode.id === List.id) {
      listViewRef.value &&
      listViewRef.value.sort(netdiskConfig.sortingConfig.sortingField.elField, netdiskConfig.sortingConfig.sortingMethod.elStr)
    }

    clearSelection()
    reload()
  }, { deep: true }
)

// const fileNameFuzzy = ref()
//
// // 搜素
// const search = () => {
//   reload()
// }

/**
 * 分享文件Ref
 */
// const shareFileRef = ref()

/**
 * 分享文件
 * @param userFileInfo
 */
// const shareFile = (userFileInfo: UserFileInfo) => {
//   if (!userFileInfo || !userFileInfo.id || !userFileInfo.name) {
//     ElMessage.warning(ResultErrorMsgEnum.ERROR_SHARE_CONTENT_EMPTY)
//     return
//   }
//   const currentShareFileIds: Array<number> = []
//   currentShareFileIds.push(userFileInfo.id)
//   openShareDialog(currentShareFileIds, userFileInfo.name)
// }

/**
 * 打开分享弹窗
 * @param ids
 * @param title
 */
// const openShareDialog = (ids: Array<number>, title: string) => {
//   shareFileRef.value.show(ids, title)
// }
</script>

<style scoped lang="scss">
@import "@/styles/file.list.scss";
</style>