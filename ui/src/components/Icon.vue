<script setup lang="ts">
import {ref, watch} from "vue";
import {getThumbnail} from "@/api/v1/file";

const props = defineProps({
  // 文件类型
  fileType: {
    type: Number
  },
  // 图标名称
  iconName: {
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
    default: "cover"
  }
});

interface FileType {
  desc: string;
  icon: string;
}

// TODO 优化
const fileTypeMap: { [key: number]: FileType | undefined } = {
  // 0: {desc: "目录", icon: "folder"},
  0: {desc: "其他文件", icon: "others"},
  1: {desc: "视频", icon: "video"},
  2: {desc: "音频", icon: "music"},
  3: {desc: "图片", icon: "image"},
  4: {desc: "exe", icon: "pdf"},
  5: {desc: "doc", icon: "word"},
  6: {desc: "excel", icon: "excel"},
  7: {desc: "纯文本", icon: "txt"},
  8: {desc: "程序", icon: "code"},
  9: {desc: "压缩包", icon: "zip"}
};

const thumbnailUrl = ref("");

const getImage = () => {
  if (props.thumbnail) {
    getThumbnail(props.thumbnail).then(({data}) => {
      thumbnailUrl.value = URL.createObjectURL(new Blob([data]));
    });

    return;
  }

  let icon = "others";

  // TODO 优化关于文件夹图标显示的逻辑
  if (props.iconName) {
    icon = props.iconName;
  } else if (props.fileType != undefined) {
    if (props.fileType == -1) {
      icon = "folder";
    } else {
      const iconMap = fileTypeMap[props.fileType];
      if (iconMap != undefined) {
        icon = iconMap["icon"];
      }
    }
  }

  thumbnailUrl.value = new URL(`/src/assets/icon-image/${icon}.png`, import.meta.url).href;
};

watch(() => props, () => {
  // 一旦props改变，就执行此代码
  getImage();
}, {immediate: true, deep: true})
</script>

<template>
<span :style="{width: width + 'px', height: width + 'px'}" class="icon">
  <img :src="thumbnailUrl" :style="{'object-fit': fit}"/>
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