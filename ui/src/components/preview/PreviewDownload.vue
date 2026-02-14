<script setup lang="ts">

import Utils from '@/utils/Utils'
import Icon from '@/components/Icon.vue'
import { useUploaderStore } from '@/stores/uploader'
import { OTHER, ZIP } from '@/enums/IconEnum'

const props = defineProps({
  fileInfo: {
    type: Object
  }
})

const uploaderStore = useUploaderStore()

const download = async () => {
  if (!props.fileInfo?.id) {
    return
  }
  await uploaderStore.startDownloadByIds([props.fileInfo.id], props.fileInfo.name)
}
</script>

<template>
  <div class="others">
    <div class="body-content">
      <div>
        <Icon :iconUrl="ZIP.suffixSet.has(fileInfo.suffix) ? ZIP.iconUrl : OTHER.iconUrl" :width="80" />
      </div>
      <div class="file-name">{{ fileInfo.name }}</div>
      <div class="tips">该类型的文件暂不支持预览，请下载后查看</div>
      <div class="download-btn">
        <el-button type="primary" @click="download">点击下载{{ Utils.sizeToStr(fileInfo.size) }}</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.others {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;

  .body-content {
    text-align: center;

    .file-name {
      font-weight: bold;
    }

    .tips {
      margin-top: 5px;
      font-size: 13px;
      color: #999898;
    }

    .download-btn {
      margin-top: 20px;
    }
  }
}
</style>