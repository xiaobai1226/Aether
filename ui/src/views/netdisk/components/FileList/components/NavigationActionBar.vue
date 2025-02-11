<script setup lang="ts">
import { ref } from 'vue'
import { useSystemStore } from '@/stores/system'
import { DisplayModeEnum } from '@/enums/DisplayModeEnum'
import Navigation from '@/components/Navigation.vue'
import { SortingMethodEnum } from '@/enums/SortingMethodEnum'
import { SortingFieldEnum } from '@/enums/SortingFieldEnum'

/**
 * 获取系统配置
 */
const systemStore = useSystemStore()

/**
 * 获取网盘配置
 */
const netdiskConfig = systemStore.netdiskConfig

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

    <span class="right-side">
       <!-- 排序方式 -->
      <el-dropdown :show-timeout="0">
        <span class="iconfont icon-paixu">
          <span class="order-txt">
            按{{ netdiskConfig.sortingConfig.sortingField.name }}{{ netdiskConfig.sortingConfig.sortingMethod.name }}
          </span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <!-- 排序字段 -->
            <el-dropdown-item v-for="item in SortingFieldEnum" :key="item.id"
                              @click="systemStore.changeSortingField(item)">
              <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                    'display-mode-item-selected':  netdiskConfig.sortingConfig.sortingField.id === item.id }" />
              <span :class="{ 'display-mode-item-selected': netdiskConfig.sortingConfig.sortingField.id === item.id }">
                {{ item.name }}
              </span>
            </el-dropdown-item>

            <!-- 排序方式 -->
            <span v-for="(item) in SortingMethodEnum" :key="item.id" @click="systemStore.changeSortingMethod(item)">
              <el-dropdown-item v-if="item.isShow" :divided="item.id === 0">
                <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                    'display-mode-item-selected': netdiskConfig.sortingConfig.sortingMethod.id === item.id }" />
                <span
                  :class="{ 'display-mode-item-selected': netdiskConfig.sortingConfig.sortingMethod.id === item.id }">
                  {{ item.name }}
                </span>
            </el-dropdown-item>
            </span>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 显示模式 -->
      <el-dropdown :show-timeout="0">
        <span class="iconfont icon-xianshimoshi" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="(item) in DisplayModeEnum" :key="item.id"
                              @click="systemStore.changeDisplayMode(item)">
              <span :class="{ 'iconfont icon-right': true, 'display-mode-item': true,
                    'display-mode-item-selected': netdiskConfig.displayMode.id === item.id }" />
              <span :class="{ 'display-mode-item-selected': netdiskConfig.displayMode.id === item.id }">
                {{ item.name }}
              </span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </span>
  </div>
</template>

<style scoped lang="scss">
.second-op {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .right-side {
    display: flex;
    align-items: center;
    gap: 30px;

    .icon-paixu {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 17px;
      cursor: pointer;

      .order-txt {
        font-size: 13px;
        margin-left: 1px;
      }
    }

    .icon-xianshimoshi {
      font-size: 18px;
      margin-right: 30px;
      cursor: pointer;
    }
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