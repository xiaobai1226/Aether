<template>
  <div class="file-list">
    <div v-for="(item, index) in taskList" :key="item.taskId" class="file-item">
      <div class="upload-panel">
        <div class="file-name">{{ item.fileName || '打包下载.zip' }}</div>
        <div class="progress">
          <el-progress :percentage="item.progress"
                       v-if="item.status === DOWNLOAD_STATUS.preparing.value || item.status === DOWNLOAD_STATUS.downloading.value || item.status === DOWNLOAD_STATUS.success.value">
          </el-progress>
        </div>
        <div class="upload-status">
          <span :class="['iconfont', 'icon-' + DOWNLOAD_STATUS[item.status].icon]"
                :style="{color: DOWNLOAD_STATUS[item.status].color}">
          </span>
          <span class="status" :style="{ color: DOWNLOAD_STATUS[item.status].color }">
            {{ item.status === DOWNLOAD_STATUS.fail.value ? item.errorMsg : DOWNLOAD_STATUS[item.status].desc }}
          </span>
          <span class="upload-info"
                v-if="item.status === DOWNLOAD_STATUS.preparing.value || item.status === DOWNLOAD_STATUS.downloading.value">
            {{ item.completedFileCount || 0 }} / {{ item.totalFileCount || 0 }} 文件
          </span>
          <span class="finish-time" v-if="item.finishTime && item.status === DOWNLOAD_STATUS.success.value">
            {{ item.finishTime }}
          </span>
        </div>
      </div>
      <div class="op">
        <div class="op-btn">
          <Icon :width="28" class="clean btn-item" :iconUrl="CLEAN.iconUrl" title="清除"
                v-if="item.status === DOWNLOAD_STATUS.success.value"
                @click="uploaderStore.clearDownloadRecord(item.taskId, index, 2)"></Icon>
          <Icon :width="28" class="clean btn-item" :iconUrl="UPLOAD.iconUrl" title="重新开始"
                v-if="item.status === DOWNLOAD_STATUS.fail.value"
                @click="uploaderStore.restartFailedDownload(item.taskId)"></Icon>
        </div>
      </div>
    </div>
    <div v-if="taskList.length == 0">
      <NoData msg="暂无下载任务"></NoData>
    </div>
  </div>
</template>

<script setup lang="ts">
import NoData from '@/components/NoData.vue'
import Icon from '@/components/Icon.vue'
import { CLEAN, UPLOAD } from '@/enums/IconEnum'
import { DOWNLOAD_STATUS, type DownloadTaskItem } from '@/views/netdisk/components/Uploader/types'
import { useUploaderStore } from '@/stores/uploader'

defineProps({
  taskList: {
    type: Array<DownloadTaskItem>,
    default: null
  },
  type: {
    type: String,
    default: ''
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
        flex-shrink: 0;
      }

      .status {
        font-size: 13px;
        min-width: 70px;
        flex-shrink: 0;
      }

      .upload-info {
        margin-left: 5px;
        font-size: 12px;
        color: rgb(112, 111, 111);
        min-width: 140px;
        font-family: 'Courier New', Consolas, monospace;
        flex-shrink: 0;
      }

      .finish-time {
        margin-left: 10px;
        font-size: 12px;
        color: #666;
        flex-shrink: 0;
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
    }
  }
}
</style>
