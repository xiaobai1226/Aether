<template>
  <div>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
            :showCannel="false" @close="dialogConfig.show=false">
      <div class="navigation-panel">
        <Navigation ref="navigationRef" @navChange="nacChange" :watchPath="false" />
      </div>
      <div ref="loadingRef">
        <div v-infinite-scroll="loadNextPage" class="folder-list" v-if="folderData.list.length > 0"
             :infinite-scroll-disabled="disabled">
          <div class="folder-item" v-for="item in folderData.list" @click="selectFolder(item)" :key="item.id">
            <Icon :fileType="-1"></Icon>
            <span class="file-name">{{ item.name }}</span>
          </div>
        </div>
        <div v-else class="tips">
          {{ dialogConfig.title }} <span>{{ currentFolderName }}</span>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import Dialog from '@/components/Dialog.vue'
import Icon from '@/components/Icon.vue'
import { getFolderListByPage } from '@/api/file'
import type {
  GetFileListByPageResponse, GetFolderListByPageRequest,
  UserFileInfo
} from '@/api/file/types'
import Navigation from '@/components/Navigation.vue'

/**
 * 配置对象
 */
const dialogConfig = ref({
  // 0 移动 1 复制
  type: 0,
  show: false,
  title: '移动到',
  buttons: [
    {
      text: '移动到此',
      type: 'primary',
      click: () => {
        folderSelect()
      }
    }
  ]
})

/**
 * 初始化列表数据
 */
const folderData = ref<GetFileListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 15,
  total: 0,
  totalPage: 0
})

/**
 * 当前目录路径
 */
const currentPath = ref<string | null>(null)

const currentFolderName = ref<string>('根目录')

/**
 * 导航Ref实例
 */
const navigationRef = ref()

/**
 * 正在加载标识
 */
const loading = ref(false)

/**
 * 加载Ref
 */
const loadingRef = ref()

/**
 * 是否没有更多
 */
const noMore = computed(() => folderData.value.list.length >= folderData.value.total)

/**
 * 是否禁用
 */
const disabled = computed(() => loading.value || noMore.value)

/**
 * 加载当前文件夹内容
 */
const loadCurrentFolder = () => {
  loading.value = true

  const getFolderListByPageRequest: GetFolderListByPageRequest = {
    pageNum: folderData.value.pageNum,
    pageSize: folderData.value.pageSize
  }

  if (currentPath.value != null) {
    getFolderListByPageRequest.path = currentPath.value as string
  }

  getFolderListByPage(getFolderListByPageRequest, loadingRef.value).then(({ data }) => {
    if (data == null) {
      if (folderData.value.pageNum === 1) {
        folderData.value = {
          list: [],
          pageNum: 1,
          pageSize: 15,
          total: 0,
          totalPage: 0
        }
      }
    } else {
      if (folderData.value.pageNum === 1) {
        folderData.value = data
      } else if (folderData.value.pageNum > 1) {
        folderData.value.totalPage = data.totalPage
        folderData.value.total = data.total
        folderData.value.list = folderData.value.list.concat(data.list)
      }
    }
    currentFolderName.value = (currentPath.value == null) ? '根目录' : (currentPath.value as string).split('/').pop() as string

    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/**
 * 重新加载
 */
const reload = () => {
  folderData.value.pageNum = 1
  loadCurrentFolder()
}

/**
 * 加载下一页
 */
const loadNextPage = () => {
  folderData.value.pageNum = folderData.value.pageNum + 1
  loadCurrentFolder()
}

/**
 * 关闭选择目录弹窗
 */
const close = () => {
  dialogConfig.value.show = false
}

/**
 * 打开选择目录弹窗
 * @param type 0 移动 1 复制
 * @param path 当前路径
 */
const showFolderDialog = (type: number, path: string | null) => {
  dialogConfig.value.type = type
  if (type == 0) {
    dialogConfig.value.title = '移动到'
    dialogConfig.value.buttons[0].text = '移动到此'
  } else if (type == 1) {
    dialogConfig.value.title = '复制到'
    dialogConfig.value.buttons[0].text = '复制到此'
  } else {
    return
  }
  currentPath.value = path
  dialogConfig.value.show = true
  nextTick(() => {
    if (navigationRef.value) {
      navigationRef.value.updateFolderList(path)
    }
    reload()
  })
}

defineExpose({ showFolderDialog, close })

/**
 * 导航改变回调
 * @param path 当前路径
 */
const nacChange = (path: string | null) => {
  currentPath.value = path
  reload()
}

/**
 * 选择目录
 * @param userFolderInfo
 */
const selectFolder = (userFolderInfo: UserFileInfo) => {
  navigationRef.value.openFolder(userFolderInfo.name)
}

const emit = defineEmits(['folderSelect'])
// 确定选择目录
const folderSelect = () => {
  emit('folderSelect', currentPath.value, dialogConfig.value.type)
}
</script>

<style scoped lang="scss">
.navigation-panel {
  padding-left: 10px;
  background: #f1f1f1;
}

.folder-list {
  .folder-item {
    cursor: pointer;
    display: flex;
    align-items: center;
    padding: 10px;

    .file-name {
      display: inline-block;
      margin-left: 10px;
    }

    &:hover {
      background: #f8f8f8;
    }
  }

  max-height: calc(30vh - 20px);
  min-height: 200px;
  overflow-y: auto;
}

.tips {
  text-align: center;
  line-height: 200px;

  span {
    color: #06a7ff;
  }
}
</style>