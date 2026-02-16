<script setup lang="ts">
import { type PropType, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getThumbnailUrl } from '@/api/v1/file'
import { IconEnum, FOLDER, OTHER } from '@/enums/IconEnum'

/**
 * 配置定义
 */
export interface IconConfig {
  // 图标宽度
  width: Number,
  // 图标圆角弧度
  borderRadius: Number,
  // 图片填充方式
  fit: String,
  // 图片宽度
  imgWidth: String,
  // 图片高度
  imgHeight: String,
  // 图片最大宽度
  imgMaxWidth?: String,
  // 图片最大高度
  imgMaxHeight?: String
}

const props = defineProps({
  // 文件类型 0 文件夹 1 文件
  itemType: {
    type: Number
  },
  // 后缀
  suffix: {
    type: String
  },
  // 图标Url
  iconUrl: {
    type: String
  },
  // 文件缩略图
  thumbnail: {
    type: String
  },
  // Icon配置
  iconConfig: {
    type: Object as PropType<IconConfig>,
    default: () => ({
      width: 32,
      borderRadius: 4,
      fit: 'cover',
      imgWidth: '100%',
      imgHeight: '100%'
    })
  },
  // 图标宽度
  width: Number,
  // 图片填充方式
  fit: String,
  // 懒加载预加载距离（IntersectionObserver rootMargin）
  lazyRootMargin: {
    type: String,
    default: '120px'
  }
})

const thumbnailUrl = ref('')
const iconRef = ref<HTMLElement>()
const canLoadThumbnail = ref(false)
let thumbnailObserver: IntersectionObserver | null = null

const clearThumbnailObserver = () => {
  if (thumbnailObserver) {
    thumbnailObserver.disconnect()
    thumbnailObserver = null
  }
}

const initThumbnailObserver = () => {
  clearThumbnailObserver()
  canLoadThumbnail.value = false

  if (!props.thumbnail) {
    canLoadThumbnail.value = true
    return
  }

  if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
    canLoadThumbnail.value = true
    return
  }

  if (!iconRef.value) {
    canLoadThumbnail.value = true
    return
  }

  thumbnailObserver = new IntersectionObserver((entries) => {
    const isVisible = entries.some(entry => entry.isIntersecting)
    if (isVisible) {
      canLoadThumbnail.value = true
      clearThumbnailObserver()
    }
  }, {
    rootMargin: props.lazyRootMargin as string
  })
  thumbnailObserver.observe(iconRef.value)
}

const getImage = () => {

  // 如果指定了iconUrl
  if (props.iconUrl) {
    thumbnailUrl.value = props.iconUrl
    return
  }

  // 如果是文件夹
  if (props.itemType === 0) {
    thumbnailUrl.value = FOLDER.iconUrl
    return
  }

  if (props.thumbnail) {
    if (canLoadThumbnail.value) {
      const nextThumbnailUrl = getThumbnailUrl(props.thumbnail as string)
      if (thumbnailUrl.value !== nextThumbnailUrl) {
        thumbnailUrl.value = nextThumbnailUrl
      }
    } else {
      // 先渲染类型图标，进入可视区再加载缩略图
      getFinalImage()
    }
    return
  }

  getFinalImage()
}

const getFinalImage = () => {
  // 如果后缀不为空
  if (props.suffix) {
    for (const key in IconEnum) {
      const item = IconEnum[key]
      if (item.suffixSet.has(props.suffix)) {
        thumbnailUrl.value = item.iconUrl
        return
      }
    }
  }

  thumbnailUrl.value = OTHER.iconUrl
}

watch(() => props, () => {
  // 一旦props改变，就执行此代码
  getImage()
}, { immediate: true, deep: true })

watch(() => props.thumbnail, () => {
  initThumbnailObserver()
  getImage()
}, { immediate: true })

watch(canLoadThumbnail, (visible) => {
  if (visible) {
    getImage()
  }
})

onMounted(() => {
  initThumbnailObserver()
})

onBeforeUnmount(() => {
  clearThumbnailObserver()
})
</script>

<template>
  <span ref="iconRef" :style="{width: (width ? width : iconConfig.width) + 'px', height: (width ? width : iconConfig.width) + 'px'}"
        class="icon">
    <el-image :src="thumbnailUrl" loading="lazy" @error="getFinalImage"
              :style="{'object-fit': (fit ? fit : iconConfig.fit), 'border-radius': iconConfig.borderRadius + 'px', width: iconConfig.imgWidth, height: iconConfig.imgHeight, 'max-width': iconConfig.imgMaxWidth, 'max-height': iconConfig.imgMaxHeight }" />
  </span>
</template>

<style scoped lang="scss">
.icon {
  text-align: center;
  display: inline-flex; /* 使用 Flexbox */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  overflow: hidden;
}
</style>