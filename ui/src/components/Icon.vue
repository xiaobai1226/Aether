<script setup lang="ts">
import { ref, watch } from 'vue'
import { getThumbnail } from '@/api/v1/file'
import { IconEnum, FOLDER, OTHER } from '@/enums/IconEnum'

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
  // 图标宽度
  width: {
    type: Number,
    default: 32
  },
  // 图片填充方式
  fit: {
    type: String,
    default: 'cover'
  }
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
    getThumbnail(props.thumbnail).then(({ data }) => {
      thumbnailUrl.value = URL.createObjectURL(new Blob([data]))
    })
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
<span :style="{width: width + 'px', height: width + 'px'}" class="icon">
  <img :src="thumbnailUrl" :style="{'object-fit': fit}" />
</span>
</template>

<style scoped lang="scss">
.icon {
  text-align: center;
  display: inline-block;
  border-radius: 3px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
  }
}
</style>