<template>
  <div class="image-viewer">
    <el-image-viewer :initial-index="previewImgIndex" hide-on-click-modal :url-list="imageList" @close="closeImgViewer"
                     v-if="previewImgIndex != null">
    </el-image-viewer>
    <!-- 文件名显示层 -->
    <div class="image-filename" v-if="previewImgIndex != null && fileName">
      <span class="filename-text">{{ fileName }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getImageUrl } from '@/api/v1/file'

const props = defineProps({
  // 文件ID
  fileId: Number,
  
  // 文件名
  fileName: {
    type: String,
    default: ''
  }
})

const imageList = ref<Array<string>>([])

/**
 * 初始预览图像索引
 */
const previewImgIndex = ref<number | null>(null)

/**
 * 文件名
 */
const fileName = ref<string>('')

/**
 * 显示预览
 * @param index
 * @param name 文件名
 */
const show = (index: number, name?: string) => {
  previewImgIndex.value = index
  fileName.value = name || props.fileName || ''
  if (props.fileId) {
    imageList.value = [getImageUrl(props.fileId)]
  }
}

defineExpose({ show })

/**
 * 关闭预览
 */
const closeImgViewer = () => {
  previewImgIndex.value = null
  fileName.value = ''
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
  
  .image-filename {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
    z-index: 2100; // 确保在 el-image-viewer 之上（el-image-viewer 的 z-index 是 2000）
    pointer-events: none; // 允许点击穿透
    
    .filename-text {
      color: #fff;
      font-size: 16px;
      font-weight: 500;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
      max-width: 80%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      padding: 0 20px;
    }
  }
}
</style>