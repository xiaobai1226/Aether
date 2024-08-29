<script setup lang="ts">
import { ref, nextTick } from 'vue'
import PreviewImage from '@/components/preview/PreviewImage.vue'
import type { UserFileInfo } from '@/api/file/types'
import PreviewVideo from '@/components/preview/PreviewVideo.vue'
import PreviewDoc from '@/components/preview/PreviewDoc.vue'
import PreviewExcel from '@/components/preview/PreviewExcel.vue'
import PreviewPdf from '@/components/preview/PreviewPdf.vue'
import PreviewTxt from '@/components/preview/PreviewTxt.vue'
import PreviewMusic from '@/components/preview/PreviewMusic.vue'
import PreviewDownload from '@/components/preview/PreviewDownload.vue'
import Window from '@/components/Window.vue'

const windowShow = ref(false)
const closeWindow = () => {
  windowShow.value = false
}

const imageViewRef = ref()

/**
 * 文件信息
 */
const fileInfo = ref<UserFileInfo>({ itemType: 1, path: '' })

/**
 * 显示预览
 * @param data
 * @param showPart
 */
const showPreview = (data: UserFileInfo, showPart) => {
  fileInfo.value = data
  if (data.category == 3) {
    nextTick(() => {
      imageViewRef.value.show(0)
    })
  } else {
    windowShow.value = true
  }
}

defineExpose({ showPreview })
</script>

<template>
  <!--  <PreviewImage v-if="fileInfo.category == 3" ref="imageViewRef" :imageList="[imageUrl]"/>-->
  <PreviewImage v-if="fileInfo.category == 3" ref="imageViewRef" :fileId="fileInfo.id" />
  <Window :show="windowShow" @close="closeWindow" :width="fileInfo.category == 1 ? 1500 : 900"
          :title="fileInfo.name" :align="fileInfo.category == 1 ? 'center' : 'top'" v-else>
    <PreviewVideo :fileId="fileInfo.id" v-if="fileInfo.category == 1" />
    <PreviewDoc :fileId="fileInfo.id" v-else-if="fileInfo.fileType == 5" />
    <PreviewExcel :fileId="fileInfo.id" v-else-if="fileInfo.fileType == 6" />
    <PreviewPdf :fileId="fileInfo.id" v-else-if="fileInfo.fileType == 4" />
    <PreviewTxt :fileId="fileInfo.id" v-else-if="fileInfo.fileType == 7 || fileInfo.fileType == 8" />
    <PreviewMusic :fileId="fileInfo.id" :fileName="fileInfo.name" v-else-if="fileInfo.category == 2" />
    <PreviewDownload :fileInfo="fileInfo" v-else />
  </Window>
</template>

<style scoped lang="scss">

</style>