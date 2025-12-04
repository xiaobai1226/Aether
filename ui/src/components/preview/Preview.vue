<script setup lang="ts">
import { ref, nextTick } from 'vue'
import PreviewImage from '@/components/preview/PreviewImage.vue'
import type { UserFileInfo } from '@/api/v1/file/types'
import PreviewVideo from '@/components/preview/PreviewVideo.vue'
import PreviewDoc from '@/components/preview/PreviewDoc.vue'
import PreviewExcel from '@/components/preview/PreviewExcel.vue'
import PreviewPdf from '@/components/preview/PreviewPdf.vue'
import PreviewTxt from '@/components/preview/PreviewTxt.vue'
import PreviewMusic from '@/components/preview/PreviewMusic.vue'
import PreviewMarkdown from '@/components/preview/PreviewMarkdown.vue'
import PreviewDownload from '@/components/preview/PreviewDownload.vue'
import Window from '@/components/Window.vue'
import { CODE, EXCEL, IMAGE, MARKDOWN, MUSIC, PDF, TXT, VIDEO, WORD } from '@/enums/IconEnum'

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
 * 获取预览窗口宽度
 */
const getPreviewWidth = () => {
  if (!fileInfo.value.suffix) return 900
  
  // 视频类文件使用最大宽度
  if (VIDEO.suffixSet.has(fileInfo.value.suffix)) {
    return 1500
  }
  
  // Markdown 文档使用较大宽度以提供更好的阅读体验
  if (MARKDOWN.suffixSet.has(fileInfo.value.suffix)) {
    return 1200
  }
  
  // PDF 文档也适合使用较大宽度
  if (PDF.suffixSet.has(fileInfo.value.suffix)) {
    return 1100
  }
  
  // 其他文件使用默认宽度
  return 900
}

/**
 * 显示预览
 * @param data
 * @param showPart
 */
const showPreview = (data: UserFileInfo, showPart) => {
  fileInfo.value = data
  if (data.suffix && IMAGE.suffixSet.has(data.suffix)) {
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
  <PreviewImage v-if="(fileInfo.suffix && IMAGE.suffixSet.has(fileInfo.suffix))" ref="imageViewRef"
                :fileId="fileInfo.id" />
  <Window :show="windowShow" @close="closeWindow"
          :width="getPreviewWidth()"
          :title="fileInfo.name" :align="(fileInfo.suffix && VIDEO.suffixSet.has(fileInfo.suffix)) ? 'center' : 'top'"
          v-else>
    <PreviewVideo :fileId="fileInfo.id" :name="fileInfo.name"
                  v-if="(fileInfo.suffix && VIDEO.suffixSet.has(fileInfo.suffix))" />
    <PreviewDoc :fileId="fileInfo.id"
                v-else-if="(fileInfo.suffix && (WORD.suffixSet.has(fileInfo.suffix)))" />
    <PreviewExcel :fileId="fileInfo.id" v-else-if="(fileInfo.suffix &&  EXCEL.suffixSet.has(fileInfo.suffix))" />
    <PreviewPdf :fileId="fileInfo.id" v-else-if="(fileInfo.suffix &&  PDF.suffixSet.has(fileInfo.suffix))" />
    <PreviewMarkdown :fileId="fileInfo.id" :fileName="fileInfo.name"
                     v-else-if="(fileInfo.suffix &&  MARKDOWN.suffixSet.has(fileInfo.suffix))" />
    <PreviewTxt :fileId="fileInfo.id"
                v-else-if="(fileInfo.suffix &&  (TXT.suffixSet.has(fileInfo.suffix) || CODE.suffixSet.has(fileInfo.suffix)))" />
    <PreviewMusic :fileId="fileInfo.id" :fileName="fileInfo.name"
                  v-else-if="(fileInfo.suffix &&  MUSIC.suffixSet.has(fileInfo.suffix))" />
    <PreviewDownload :fileInfo="fileInfo" v-else />
  </Window>
</template>

<style scoped lang="scss">

</style>