<template>
  <div class="image-viewer">
    <el-image-viewer :initial-index="previewImgIndex" hide-on-click-modal :url-list="imageList" @close="closeImgViewer"
                     v-if="previewImgIndex != null">
    </el-image-viewer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getImageUrl } from '@/api/file'

const props = defineProps({
  // // 图像列表
  // imageList: {
  //   type: Array<string>
  // },

  // 文件ID
  fileId: Number
})

const imageList = ref<Array<string>>([])

/**
 * 初始预览图像索引
 */
const previewImgIndex = ref<number | null>(null)

/**
 * 显示预览
 * @param index
 */
const show = (index: number, fileId: number) => {
  // stop();
  previewImgIndex.value = index
  if (props.fileId) {
    imageList.value = [getImageUrl(props.fileId)]
  }
}

defineExpose({ show })

/**
 * 关闭预览
 */
const closeImgViewer = () => {
  // startScroll();
  previewImgIndex.value = null
}

// const stopScroll = () => {
//   document.body.style.overflow = "hidden";
// };
//
// const startScroll = () => {
//   document.body.style.overflow = "auto";
// };

// const getFullImage = () => {
//   if (props.fileId) {
//     getImage(props.fileId).then(async ({ data }) => {
//       const blobToBase64 = await Utils.blobToBase64(new Blob([data]))
//       imageList.value = [blobToBase64]
//     })
//   }
// }
</script>

<style scoped lang="scss">
.image-viewer {
  .el-image-viewer__mask {
    opacity: 0.7;
  }
}
</style>