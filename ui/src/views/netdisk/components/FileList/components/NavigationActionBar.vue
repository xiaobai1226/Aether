<script setup lang="ts">
import { ref } from 'vue'
import { useSystemStore } from '@/stores/system'
import { DisplayModeEnum } from '@/enums/DisplayModeEnum'
import Navigation from '@/components/Navigation.vue'

/**
 * 父类回调方法
 */
const emit = defineEmits(['change-display-mode'])

const changeDisplayMode = (mode: DisplayModeEnum) => {
  emit('change-display-mode', mode)
}


// 获取系统配置
const systemStore = useSystemStore()

/**
 * 导航Ref实例
 */
const navigationRef = ref()

/**
 * 打开文件夹
 */
const openFolder = (name: string) => {
  navigationRef.value.openFolder(name)
}

/**
 * 更新目录集合
 * @param path 当前路径
 */
const updateFolderList = (path: string | null) => {
  navigationRef.value.updateFolderList(path)
}

/**
 * 将子组件暴露出去，否则父组件无法调用
 */
defineExpose({ openFolder, updateFolderList })
</script>

<template>
  <div class="second-op">
    <!-- 导航 -->
    <!--        <Navigation ref="navigationRef" @navChange="navChange"/>-->
    <Navigation ref="navigationRef" class="navigation" />
    <el-dropdown>
      <span class="iconfont icon-xianshimoshi" />
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="changeDisplayMode(DisplayModeEnum.List)">
                <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                  'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.List }" />
            <span
              :class="{ 'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.List }">列表模式</span>
          </el-dropdown-item>
          <el-dropdown-item @click="changeDisplayMode(DisplayModeEnum.Thumbnail)">
                  <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                  'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.Thumbnail }" />
            <span
              :class="{ 'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.Thumbnail }">缩略模式</span>
          </el-dropdown-item>
          <el-dropdown-item @click="changeDisplayMode(DisplayModeEnum.Large)">
                 <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                 'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.Large }" />
            <span
              :class="{ 'display-mode-item-selected': systemStore.displayMode === DisplayModeEnum.Large }">大图模式</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped lang="scss">
.second-op {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .icon-xianshimoshi {
    font-size: 20px;
    margin-right: 20px;
    cursor: pointer;
  }
}

.navigation {
  margin-top: 10px;
  margin-bottom: 10px;
}

.display-mode-item {
  margin-right: 3px;
  font-weight: bold;
  visibility: hidden;
}

.display-mode-item-selected {
  visibility: visible;
  color: #5b9df8;
}
</style>