<template>
  <div>
    <div class="top">
      <div class="top-op">
        <div class="btn" v-show="selectedFileIds.length == 0">
          <el-button type="primary" @click="openUploadPopup">
            <span class="iconfont icon-upload"></span>
            上传
          </el-button>
          <UploadPopup ref="uploadPopupRef" :category="currentCategory" :path="currentPath"
                       :callbackFunction="reload" />
        </div>
        <el-button type="success" v-show="selectedFileIds.length == 0" @click="showEditPanel(-1)">
          <span class="iconfont icon-folder-add"></span>
          新建文件夹
        </el-button>

        <el-dropdown class="dropdown_download" placement="bottom" v-show="selectedFileIds.length > 0">
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

        <!--        <el-button type="primary" v-show="selectedFileIds.length > 0" @click="shareFileBatch">-->
        <!--          <span class="iconfont icon-share"></span>-->
        <!--          分享-->
        <!--        </el-button>-->
        <el-button type="danger" v-show="selectedFileIds.length > 0" @click="delFileBatch">
          <span class="iconfont icon-delete"></span>
          删除
        </el-button>
        <el-button type="warning" v-show="selectedFileIds.length > 0" @click="moveFileBatch">
          <span class="iconfont icon-move"></span>
          移动
        </el-button>
        <el-button type="warning" v-show="selectedFileIds.length > 0" @click="copyFileBatch">
          <span class="iconfont icon-copy"></span>
          复制
        </el-button>
        <!--        <div class="search-panel">-->
        <!--          <el-input clearable placeholder="输入文件名搜索" v-model="fileNameFuzzy" @keyup.enter="search">-->
        <!--            <template #suffix>-->
        <!--              <i class="iconfont icon-search" @click="search"></i>-->
        <!--            </template>-->
        <!--          </el-input>-->
        <!--        </div>-->
        <div class="iconfont icon-refresh" @click="reload"></div>
      </div>
      <!-- 导航 -->
      <div>
        <!--        <Navigation ref="navigationRef" @navChange="navChange"/>-->
        <Navigation ref="navigationRef" />
      </div>
      <div class="total_number">共 {{ tableData.total }} 项</div>
    </div>

    <div ref="loadingRef">
      <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
               :initFetch="false" :options="tableOptions" :loading="loading" @rowSelected="rowSelected">
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
              <span class="file-name" :title="row.name" v-if="showEditPanelIndex != index">
              <span @click="preview(row)">{{ row.name }}</span>
                <!-- TODO 需要删除 -->
                <!--              <span v-if="row.status == 0" class="transfer-status">转码中</span>-->
                <!--              <span v-if="row.status == 1" class="transfer-status transfer-fail">转码失败</span>-->
            </span>
              <!-- 新建文件夹或重命名输入栏 -->
              <div class="edit-panel" v-if="showEditPanelIndex == index">
                <el-input v-model.trim="editPanelFileName" ref="editPanelRef" @blur="hideEditPanel(index)"
                          @keyup.enter="submitEditPanel(index)">
                </el-input>
                <span :class="['iconfont icon-right', editPanelFileName ? '' : 'not-allow']"
                      @click="submitEditPanel(index)" @mousedown="submitEditPanelMouseDown"
                      @mouseup="submitEditPanelMouseUp"></span>
                <span class="iconfont icon-error"></span>
              </div>
              <!-- 操作栏 -->
              <span class="op">
              <template v-if="showActionBarIndex == index && row.id && row.fileStatus == 1">
<!--                <span class="iconfont icon-share" @click="shareFile(row)">分享</span>-->
                <span class="iconfont icon-download" @click="download(row)">下载</span>
                <span class="iconfont icon-delete" @click="delFile(row)">删除</span>
                <span class="iconfont icon-edit" @click="showEditPanel(index)">重命名</span>
                <span class="iconfont icon-move" @click="moveFile(row)">移动</span>
                <span class="iconfont icon-copy" @click="copyFile(row)">复制</span>
              </template>
            </span>
            </div>
          </template>
          <template #fileSize="{ index, row }">
            <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
            <span v-else>-</span>
          </template>
        </Table>
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

    <FolderSelect ref="folderSelectRef" @folderSelect="handleMoveOrCopyCallback"></FolderSelect>
    <!-- 预览 -->
    <Preview ref="previewRef"></Preview>
    <!-- 分享 -->
    <ShareFile ref="shareFileRef"></ShareFile>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { getFileListByPage, newFolder, rename, del, move, copy, createDownloadSign } from '@/api/file'
import type {
  GetFileListByPageRequest,
  GetFileListByPageResponse,
  UserFileInfo,
  NewFolderRequest,
  FileRenameRequest,
  MoveRequest,
  CopyRequest, DeleteRequest
} from '@/api/file/types'
import Table from '@/components/Table.vue'
import UploadPopup from '@/components/UploadPopup.vue'
import type { Column } from '@/components/Table.vue'
import Utils from '@/utils/Utils'
import { ElMessage } from 'element-plus'
import Icon from '@/components/Icon.vue'
import Confirm from '@/utils/Confirm'
import FolderSelect from '@/components/FolderSelect.vue'
import Navigation from '@/components/Navigation.vue'
import { useRoute, useRouter } from 'vue-router'
import Preview from '@/components/preview/Preview.vue'
import ShareFile from '@/views/netdisk/components/ShareFile/index.vue'
import { useUserStore } from '@/stores/user'
import { RegexEnum } from '@/enums/RegexEnum'
import { ResultErrorMsgEnum } from '@/enums/ResultErrorMsgEnum'

// 从pinia获取用户数据
const userStore = useUserStore()

const route = useRoute()
const router = useRouter()

/**
 * 列表列定义
 */
const columns: Column[] = [
  {
    label: '文件名',
    prop: 'fileName',
    scopedSlots: 'fileName'
  },
  {
    label: '修改时间',
    prop: 'updateTime',
    width: 200
  },
  {
    label: '文件大小',
    prop: 'fileSize',
    scopedSlots: 'fileSize',
    width: 200
  }
]

/**
 * 初始化列表数据
 */
const tableData = ref<GetFileListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 30,
  total: 0,
  totalPage: 0
})

const tableOptions = ref({
  extHeight: 50,
  selectType: 'checkbox'
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
 * 导航Ref实例
 */
const navigationRef = ref()

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
    category: currentCategory.value
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
          pageSize: 30,
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
      if (navigationRef.value) {
        navigationRef.value.updateFolderList(currentPath.value)
      }
    })

    // 加载数据
    reload()
  },
  { immediate: true }
)

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
 * 编辑行状态 true 为正在编辑
 */
const editing = ref<boolean>(false)

/**
 * 新建文件夹或重命名输入框
 */
const editPanelRef = ref()

/**
 * 新建文件夹或重命名输入框的文件名
 */
const editPanelFileName = ref<string | null>(null)

/**
 * 显示新建文件夹或重命名输入框的索引 -1 为不展示，其他为要展示行的索引
 */
const showEditPanelIndex = ref<number>(-1)

/**
 * 展示新建文件夹或重命名输入框
 * @param index 对应行索引，-1 为新建文件夹
 */
const showEditPanel = (index: number) => {
  // 如果是新建文件夹
  if (index === -1) {
    // 如果已经处于编辑行状态，则直接return，否则改为正在编辑状态
    if (editing.value) {
      return
    } else {
      editing.value = true
    }

    // 将操作栏栏隐藏
    hideActionBar()

    // 向数组开头添加一个新元素，并返回数组长度
    tableData.value.list.unshift({ itemType: 0, path: currentPath.value || '/' })
    // 展示新建文件夹输入框
    showEditPanelIndex.value = 0
    nextTick(() => {
      // 光标聚焦
      if (editPanelRef.value) {
        editPanelRef.value.focus()
      }
    })
  } else {
    // 如果目前存在新建文件夹，则删除第一个元素
    if (!tableData.value.list[0].id) {
      tableData.value.list.splice(0, 1)
      index = index - 1
      // 隐藏原本的EditPanel
      hideEditPanel(0)
    }

    let currentData = tableData.value.list[index]
    // 展示重命名输入框
    showEditPanelIndex.value = index
    let selectEndIndex = 0

    if (currentData.name) {
      selectEndIndex = currentData.name.length
      editPanelFileName.value = currentData.name

      // 编辑文件，如果是文件则处理后缀
      if (currentData.itemType == 1) {
        let lastIndex = currentData.name.lastIndexOf('.')
        if (lastIndex != -1) {
          selectEndIndex = lastIndex
        }
      }
    }

    editing.value = true
    nextTick(() => {
      // 光标聚焦
      if (editPanelRef.value) {
        editPanelRef.value.focus()

        const inputDOM = editPanelRef.value.$el.querySelector('input')
        inputDOM.setSelectionRange(0, selectEndIndex)
      }
    })
  }
}

const isClickSubmitEditPanel = ref(false)

/**
 * 隐藏新建文件夹或重命名输入框
 * @param index 对应行索引
 */
const hideEditPanel = (index: number) => {
  if (isClickSubmitEditPanel.value) {
    return
  }

  // 获取对应行的数据
  const fileData: UserFileInfo = tableData.value.list[index]
  // 如果id不为空则为重命名，否则为新建文件夹
  if (!fileData.id) {
    // 删除对应行数据
    tableData.value.list.splice(index, 1)
  }
  showEditPanelIndex.value = -1
  editPanelFileName.value = null
  editing.value = false
}

/**
 * 按下提交新建文件夹或重命名请求按鈕
 */
const submitEditPanelMouseDown = () => {
  isClickSubmitEditPanel.value = true
}

/**
 * 抬起提交新建文件夹或重命名请求按鈕
 */
const submitEditPanelMouseUp = () => {
  isClickSubmitEditPanel.value = false
}

/**
 * 提交新建文件夹或重命名请求
 */
const submitEditPanel = (index: number) => {
  // 校验名称格式
  const regex = new RegExp(RegexEnum.REGEX_FILE_NAME)
  let checkResult = true
  let warnInfo = ''
  if (!editPanelFileName.value) {
    checkResult = false
    warnInfo = ResultErrorMsgEnum.ERROR_FILE_NAME_EMPTY
  } else if ((editPanelFileName.value as string).length > 100) {
    checkResult = false
    warnInfo = ResultErrorMsgEnum.ERROR_FILE_NAME_LENGTH as string
  } else if (!regex.test((editPanelFileName.value as string))) {
    checkResult = false
    warnInfo = ResultErrorMsgEnum.ERROR_FILE_NAME_FORMAT
  }

  if (!checkResult) {
    // 光标聚焦
    if (editPanelRef.value) {
      editPanelRef.value.focus()
    }

    ElMessage.warning(warnInfo)
    return
  }

  // 获取行数据
  const fileData: UserFileInfo = tableData.value.list[index]

  // 如果id不为空则为重命名，否则为新建文件夹
  if (fileData.id) {
    // 重命名
    const data: FileRenameRequest = {
      id: fileData.id,
      newName: editPanelFileName.value as string
    }

    rename(data).then(() => {
      editing.value = false
      hideEditPanel(index)
      // 重新加载数据
      reload()
    }).catch(() => {
      // 光标聚焦
      if (editPanelRef.value) {
        editPanelRef.value.focus()
      }
    })
  }
  // 新建文件夹
  else {
    const data: NewFolderRequest = {
      folderName: editPanelFileName.value as string
    }

    if (currentPath.value != null) {
      data.path = currentPath.value as string
    }

    newFolder(data).then(() => {
      editing.value = false
      hideEditPanel(0)
      // 重新加载数据
      reload()
    }).catch(() => {
      // 光标聚焦
      if (editPanelRef.value) {
        editPanelRef.value.focus()
      }
    })
  }
}

/**
 * 选中的项ID
 */
const selectedFileIds = ref<number[]>([])

/**
 * 选中行方法
 * @param rows 选中的数据项
 */
const rowSelected = (rows: UserFileInfo[]) => {
  selectedFileIds.value = []
  rows.forEach((item) => {
    if (item.id) {
      selectedFileIds.value.push(item.id)
    }
  })
}

/**
 * 文件夹选择Ref
 */
const folderSelectRef = ref()

/**
 * 当前要移动、复制的文件
 */
const currentMoveOrCopyFileIds = ref<Array<number>>([])

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
  folderSelectRef.value.showFolderDialog(0)
}

/**
 * 批量移动文件
 */
const moveFileBatch = () => {
  if (selectedFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_MOVE_CONTENT_EMPTY)
    return
  }
  currentMoveOrCopyFileIds.value = []
  currentMoveOrCopyFileIds.value = currentMoveOrCopyFileIds.value.concat(selectedFileIds.value)
  folderSelectRef.value.showFolderDialog(0)
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
    // 重新加载数据
    reload()

    // 关闭选择文件夹弹窗
    folderSelectRef.value.close()

    selectedFileIds.value = []
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
  folderSelectRef.value.showFolderDialog(1)
}

/**
 * 批量复制文件
 */
const copyFileBatch = () => {
  if (selectedFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_COPY_CONTENT_EMPTY)
    return
  }
  currentMoveOrCopyFileIds.value = []
  currentMoveOrCopyFileIds.value = currentMoveOrCopyFileIds.value.concat(selectedFileIds.value)
  folderSelectRef.value.showFolderDialog(1)
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

    selectedFileIds.value = []

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
 * 批量删除选中的文件
 */
const delFileBatch = () => {
  if (selectedFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DEL_CONTENT_EMPTY)
    return
  }
  let currentDelFileIds: Array<number> = []
  currentDelFileIds = currentDelFileIds.concat(selectedFileIds.value)

  const message = '你确定要删除所选的文件吗？删除的文件可在10天内通过回收站还原'

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
      reload()
    })
  })
}

/**
 * 分享文件Ref
 */
const shareFileRef = ref()

/**
 * 分享文件
 * @param userFileInfo
 */
const shareFile = (userFileInfo: UserFileInfo) => {
  if (!userFileInfo || !userFileInfo.id || !userFileInfo.name) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_SHARE_CONTENT_EMPTY)
    return
  }
  const currentShareFileIds: Array<number> = []
  currentShareFileIds.push(userFileInfo.id)
  openShareDialog(currentShareFileIds, userFileInfo.name)
}

/**
 * 批量分享文件
 */
const shareFileBatch = () => {
  if (selectedFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_SHARE_CONTENT_EMPTY)
    return
  }
  let currentShareFileIds: Array<number> = []
  currentShareFileIds = currentShareFileIds.concat(selectedFileIds.value)

  let title = ''
  tableData.value.list.forEach((item) => {
    if (selectedFileIds.value[0] == item.id) {
      title = item.name + '等'
    }
  })

  openShareDialog(currentShareFileIds, title)
}

/**
 * 打开分享弹窗
 * @param ids
 * @param title
 */
const openShareDialog = (ids: Array<number>, title: string) => {
  shareFileRef.value.show(ids, title)
}

const previewRef = ref()

const uploadPopupRef = ref()

const openUploadPopup = () => {
  uploadPopupRef.value.show()
}

/**
 * 预览
 * @param row
 */
const preview = (row: UserFileInfo) => {
  // 目录
  if (row.itemType == 0) {
    navigationRef.value.openFolder(row.name)
    return
  }

  previewRef.value.showPreview(row, 0)
}

const fileNameFuzzy = ref()

// 搜素
const search = () => {
  reload()
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
 * 批量下载选中的文件
 * @param type 下载类型 1 批量下载 2 打包下载
 */
const downloadBatch = (type: number) => {
  if (selectedFileIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DOWNLOAD_CONTENT_EMPTY)
    return
  }
  let currentDownloadFileIds: Array<number> = []
  currentDownloadFileIds = currentDownloadFileIds.concat(selectedFileIds.value)

  handleDownload(currentDownloadFileIds, type)
}

/**
 * 删除文件
 *
 * @param currentDownloadFileIds 要下载的文件ID集合
 * @param type 下载类型 1 批量下载 2 打包下载
 */
const handleDownload = (currentDownloadFileIds: Array<number>, type: number) => {
  if (type === 1) {
    currentDownloadFileIds.forEach((id => {
      createDownloadSign(id.toString()).then(({ data }) => {
        window.open(import.meta.env.VITE_HTTP_BASE_URL + '/file/download?sign=' + data)
      })
    }))
  } else if (type === 2) {
    createDownloadSign(currentDownloadFileIds.join(',')).then(({ data }) => {
      window.open(import.meta.env.VITE_HTTP_BASE_URL + '/file/download?sign=' + data)
    })
  }
}
</script>

<style scoped lang="scss">
@import "@/styles/file.list.scss";

.dropdown_download {
  margin-right: 12px;
}

.total_number {
  font-size: 12px;
  color: #25262BB8;
  margin-bottom: 5px;
}
</style>