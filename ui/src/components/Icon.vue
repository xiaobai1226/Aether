<script setup lang="ts">
import { type PropType, ref, watch } from 'vue'
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
  fit: String
})

const thumbnailUrl = ref('')

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
    // getThumbnail(props.thumbnail).then(({ data }) => {
    //   thumbnailUrl.value = URL.createObjectURL(new Blob([data]))
    // })

    thumbnailUrl.value = getThumbnailUrl(props.thumbnail)
    return
  }

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
</script>

<template>
<span :style="{width: (width ? width : iconConfig.width) + 'px', height: (width ? width : iconConfig.width) + 'px'}"
      class="icon">
  <img :src="thumbnailUrl"
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