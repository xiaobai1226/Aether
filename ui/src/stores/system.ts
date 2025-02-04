/**
 * 管理系统相关数据
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { List, type DisplayMode } from '@/enums/DisplayModeEnum'
import { Name, type SortingField } from '@/enums/SortingFieldEnum'
import { ASC, type SortingMethod } from '@/enums/SortingMethodEnum'

export const useSystemStore = defineStore('system', () => {

  /**
   * 网盘相关配置
   */
  const netdiskConfig = ref({
    /**
     * 显示模式
     */
    displayMode: ref<DisplayMode>(Object.assign({}, List)),

    /**
     * 排序配置
     */
    sortingConfig: ref({
      /**
       * 排序字段
       */
      sortingField: ref<SortingField>(Object.assign({}, Name)),

      /**
       * 排序方式 默认升序
       */
      sortingMethod: ref<SortingMethod>(Object.assign({}, ASC))
    })
  })

  /**
   * 修改显示模式
   */
  const changeDisplayMode = (mode: DisplayMode) => {
    netdiskConfig.value.displayMode = mode
  }

  /**
   * 修改排序字段
   */
  const changeSortingField = (sortingField: SortingField) => {
    netdiskConfig.value.sortingConfig.sortingField = sortingField
  }

  /**
   * 修改排序方式
   */
  const changeSortingMethod = (sortingMethod: SortingMethod) => {
    netdiskConfig.value.sortingConfig.sortingMethod = sortingMethod
  }

  return { netdiskConfig, changeDisplayMode, changeSortingField, changeSortingMethod }
}, {
  persist: true
})