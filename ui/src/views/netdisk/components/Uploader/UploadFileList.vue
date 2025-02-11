<template>
  <div class="file-list">
    <div v-for="(item, index) in fileList" :key="index" class="file-item">
      <div class="upload-panel">
        <div class="file-name">
          {{ item.fileName }}
        </div>
        <div class="progress">
          <el-progress :percentage="item.uploadProgress"
                       v-if="item.status == STATUS.uploading.value || item.status == STATUS.upload_seconds.value || item.status == STATUS.upload_finish.value|| item.status == STATUS.pause.value">
          </el-progress>
        </div>
        <div class="upload-status">
          <!-- 图标 -->
          <span :class="['iconfont', 'icon-' + STATUS[item.status].icon]"
                :style="{color: STATUS[item.status].color}">
            </span>
          <!-- 状态描述 -->
          <span class="status" :style="{ color: STATUS[item.status].color }">
              {{ item.status == 'fail' ? item.errorMsg : STATUS[item.status].desc }}
            </span>
          <!-- 上传中 -->
          <span class="upload-info"
                v-if="item.status === STATUS.uploading.value || item.status === STATUS.pause.value">
              {{ Utils.sizeToStr(item.uploadedSize) }} / {{ Utils.sizeToStr(item.totalSize) }}
            </span>
        </div>
      </div>
      <div class="op">
        <!-- MD5 -->
        <el-progress type="circle" :width="50" :percentage="item.md5Progress"
                     v-if="item.status == STATUS.init.value"></el-progress>
        <div class="op-btn">
          <Icon :width="28" class="btn-item" :iconUrl="PAUSE.iconUrl" title="暂停"
                v-if="item.status === STATUS.uploading.value"
                @click="uploaderStore.pauseUpload(item.uid)"></Icon>
          <span v-if="item.status === STATUS.pause.value">
              <Icon :width="28" class="btn-item" :iconUrl="UPLOAD.iconUrl"
                    v-if="item.status === STATUS.pause.value" title="上传"
                    @click="uploaderStore.startUpload(item.uid, 1)"></Icon>
              <Icon :width="28" class="del btn-item" :iconUrl="DELETE.iconUrl" title="取消"
                    v-if="item.status === STATUS.pause.value"
                    @click="uploaderStore.cancelUpload(item.uid)"></Icon>
            </span>
          <Icon :width="28" class="clean btn-item" :iconUrl="CLEAN.iconUrl" title="清除"
                v-if="item.status == STATUS.upload_finish.value || item.status == STATUS.upload_seconds.value"
                @click="uploaderStore.clearUploadRecord(item.uid, index, 2)"></Icon>
          <Icon :width="28" class="clean btn-item" :iconUrl="UPLOAD.iconUrl" title="重新开始"
                v-if="item.status === STATUS.cancel.value"
                @click="uploaderStore.startUpload(item.uid,2)"></Icon>
        </div>
      </div>
    </div>
    <div v-if="fileList.length == 0">
      <NoData msg="暂无上传任务"></NoData>
    </div>
  </div>
</template>

<script setup lang="ts">
import Utils from '@/utils/Utils'
import NoData from '@/components/NoData.vue'
import Icon from '@/components/Icon.vue'
import type { UploadFileItem } from '@/views/netdisk/components/Uploader/types'
import { STATUS } from '@/views/netdisk/components/Uploader/types'
import { useUploaderStore } from '@/stores/uploader'
import { CLEAN, DELETE, PAUSE, UPLOAD } from '@/enums/IconEnum'

const props = defineProps({
  fileList: {
    type: Array<UploadFileItem>,
    default: null
  }
})

const uploaderStore = useUploaderStore()
</script>


<style scoped lang="scss">
.file-list {
  overflow: auto;
  padding: 10px 0;
  min-height: calc(100vh / 2);
  max-height: calc(100vh - 120px);

  .file-item {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 3px 10px;
    background-color: #fff;
    border-bottom: 1px solid #ddd;
  }

  .file-item:nth-child(even) {
    background-color: #fcf8f4;
  }

  .upload-panel {
    flex: 1;

    .file-name {
      color: rgb(64, 62, 62);
    }

    .upload-status {
      display: flex;
      align-items: center;
      margin-top: 5px;

      .iconfont {
        margin-right: 3px;
      }

      .status {
        color: red;
        font-size: 13px;
      }

      .upload-info {
        margin-left: 5px;
        font-size: 12px;
        color: rgb(112, 111, 111);
      }
    }

    .progress {
      height: 10px;
    }
  }

  .op {
    width: 100px;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    .op-btn {
      .btn-item {
        cursor: pointer;
      }

      .del,
      .clean {
        margin-left: 5px;
      }
    }
  }
}
</style>