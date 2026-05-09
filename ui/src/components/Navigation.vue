<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  // 是否监听路由
  watchPath: {
    type: Boolean,
    default: true
  }
})

/**
 * 目录集合
 */
const folderList = ref<Array<string>>([])

// 打开文件夹
const openFolder = (name: string) => {
  folderList.value.push(name)
  setPath()
}

/**
 * 更新目录集合
 * @param path 当前路径
 */
const updateFolderList = (path: string | null) => {
  if (!path) {
    folderList.value = []
  } else {
    folderList.value = path.split('/').filter(Boolean)
  }
}

defineExpose({ openFolder, updateFolderList })

// 返回上一级
const backParent = () => {
  setCurrentFolder(folderList.value.length - 2)
}

// 点击导航，设置当前目录
const setCurrentFolder = (index: number) => {
  if (index == -1) {
    // 返回全部
    folderList.value = []
  } else {
    folderList.value.splice(index + 1)
  }
  setPath()
}

const emit = defineEmits(['navChange'])
const doCallback = () => {
  let path = null
  if (folderList.value.length > 0) {
    path = '/' + folderList.value.join('/')
  }
  emit('navChange', path)
}

/**
 * 设置路径
 */
const setPath = () => {
  if (!props.watchPath) {
    doCallback()
    return
  }

  if (folderList.value.length == 0) {
    router.push({ path: route.path })
  } else {
    router.push({
      path: route.path,
      query: { path: '/' + folderList.value.join('/') }
    })
  }
}
</script>

<template>
  <div class="top-navigation">
    <template v-if="folderList.length > 0">
      <span class="back link" @click="backParent">返回上一层</span>
      <el-divider direction="vertical"></el-divider>
    </template>
    <span v-if="folderList.length == 0" class="all-file">全部文件</span>
    <span v-if="folderList.length > 0" class="link" @click="setCurrentFolder(-1)">全部文件</span>
    <template v-for="(name, index) in folderList" :key="index">
      <span class="iconfont icon-right-arrow"></span>
      <span class="link" v-if="index < folderList.length - 1" @click="setCurrentFolder(index)">
        <el-tooltip :content="name" placement="bottom" :show-after="500" popper-class="nav-tooltip">
          <div class="nav-text-wrapper">{{ name }}</div>
        </el-tooltip>
      </span>
      <span v-if="index == folderList.length - 1" class="text">
        <el-tooltip :content="name" placement="bottom" :show-after="500" popper-class="nav-tooltip">
          <div class="nav-text-wrapper">{{ name }}</div>
        </el-tooltip>
      </span>
    </template>
  </div>
</template>

<style scoped lang="scss">
.top-navigation {
  font-size: 13px;
  display: flex;
  align-items: center;
  line-height: 25px;
  flex-wrap: wrap;

  .all-file {
    font-weight: bold;
  }

  .link {
    color: #06a7ff;
    cursor: pointer;
    max-width: 120px;
    box-sizing: border-box;
    display: inline-block;
  }

  .text {
    max-width: 120px;
    box-sizing: border-box;
    display: inline-block;
  }

  .nav-text-wrapper {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    width: 100%;
  }

  .icon-right-arrow {
    color: #06a7ff;
    padding: 0 5px;
    font-size: 13px;
  }
}
</style>

<style>
.nav-tooltip {
  max-width: none !important;
  background-color: white !important;
  color: #333 !important;
  border: 1px solid #e4e7ed !important;
  padding: 5px 10px !important;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1) !important;
}

.nav-tooltip .el-popper__arrow::before {
  background-color: white !important;
  border-color: #e4e7ed !important;
}
</style>