<template>
  <div class="image-viewer">
    <el-image-viewer :initial-index="previewImgIndex" hide-on-click-modal :url-list="imageList" 
                     @close="closeImgViewer" @switch="handleSwitch"
                     v-if="previewImgIndex != null">
    </el-image-viewer>
    <!-- 文件名显示层 -->
    <div class="image-filename" v-if="previewImgIndex != null && fileName">
      <span class="filename-text">{{ fileName }}</span>
      <span class="image-counter" v-if="imageList.length > 1">
        {{ currentDisplayIndex + 1 }} / {{ imageList.length }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getImageUrl } from '@/api/v1/file'
import type { UserFileInfo } from '@/api/v1/file/types'

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
 * 当前显示的图片索引（用于显示计数器）
 */
const currentDisplayIndex = ref<number>(0)

/**
 * 文件名
 */
const fileName = ref<string>('')

/**
 * 当前目录的所有图片文件列表
 */
const imageFiles = ref<UserFileInfo[]>([])

/**
 * 当前图片文件ID列表（用于文件名映射）
 */
const imageFileIds = ref<number[]>([])

/**
 * 显示预览
 * @param index
 * @param name 文件名
 * @param files 当前目录的所有图片文件列表
 * @param currentFileId 当前要预览的文件ID
 */
const show = (index: number, name?: string, files?: UserFileInfo[], currentFileId?: number) => {
  fileName.value = name || props.fileName || ''
  
  if (files && files.length > 0 && currentFileId) {
    // 保存图片文件列表
    imageFiles.value = files
    
    // 生成图片URL列表
    imageList.value = files.map(file => file.id ? getImageUrl(file.id) : '').filter(url => url)
    
    // 保存文件ID列表
    imageFileIds.value = files.map(file => file.id || 0).filter(id => id > 0)
    
    // 找到当前文件在列表中的索引
    const currentIndex = imageFileIds.value.indexOf(currentFileId)
    previewImgIndex.value = currentIndex >= 0 ? currentIndex : 0
    currentDisplayIndex.value = currentIndex >= 0 ? currentIndex : 0
    
    // 如果找到了索引，更新文件名为对应的文件名
    if (currentIndex >= 0 && files[currentIndex]) {
      fileName.value = files[currentIndex].name || fileName.value
    }
  } else {
    // 只预览单张图片
    previewImgIndex.value = index
    currentDisplayIndex.value = index
    imageFiles.value = []
    imageFileIds.value = []
    if (props.fileId) {
      imageList.value = [getImageUrl(props.fileId)]
    }
  }
}

/**
 * 处理图片切换事件
 * @param newIndex 新的索引
 */
const handleSwitch = (newIndex: number) => {
  currentDisplayIndex.value = newIndex
  // 更新文件名
  if (imageFiles.value.length > 0 && imageFiles.value[newIndex]) {
    fileName.value = imageFiles.value[newIndex].name || ''
  }
}

defineExpose({ show })

/**
 * 关闭预览
 */
const closeImgViewer = () => {
  previewImgIndex.value = null
  currentDisplayIndex.value = 0
  fileName.value = ''
  imageFiles.value = []
  imageFileIds.value = []
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
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
    z-index: 2100; // 确保在 el-image-viewer 之上（el-image-viewer 的 z-index 是 2000）
    pointer-events: none; // 允许点击穿透
    gap: 4px;
    
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
    
    .image-counter {
      color: #fff;
      font-size: 14px;
      font-weight: 400;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
      opacity: 0.9;
    }
  }
}
</style>