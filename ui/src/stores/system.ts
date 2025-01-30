// 管理系统相关数据
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { DisplayModeEnum } from '@/enums/DisplayModeEnum'

export const useSystemStore = defineStore('system', () => {
  /**
   * 显示模式
   */
  const displayMode = ref<DisplayModeEnum>(DisplayModeEnum.List)

  /**
   * 修改显示模式
   */
  const changeDisplayMode = (mode: DisplayModeEnum) => {
    displayMode.value = mode
  }

  return { displayMode, changeDisplayMode }
}, {
  persist: true
})