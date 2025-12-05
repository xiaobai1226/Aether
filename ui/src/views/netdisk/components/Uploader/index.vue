<template>
  <div class="uploader-panel">
    <div class="uploader-title">
      <div class="title-left">
        <span>上传列表</span>
        <span class="tips">（仅展示本次上传任务）</span>
      </div>
      <div class="batch-operations">
        <!-- 上传中的批量操作 -->
        <template v-if="activeName === 'uploading' && uploaderStore.uploadingFileList.length > 0">
          <el-button class="action-btn start-btn" size="small" plain @click.stop="handleStartAll">
            <el-icon><VideoPlay /></el-icon>
            <span>全部开始</span>
          </el-button>
          <el-button class="action-btn pause-btn" size="small" plain @click.stop="handlePauseAll">
            <el-icon><VideoPause /></el-icon>
            <span>全部暂停</span>
          </el-button>
          <el-button class="action-btn cancel-btn" size="small" plain @click.stop="handleCancelAll" v-if="hasPausedFiles">
            <el-icon><Close /></el-icon>
            <span>全部取消</span>
          </el-button>
        </template>
        <!-- 上传完成的批量操作 -->
        <template v-if="activeName === 'uploadSuccess' && uploaderStore.uploadSuccessFileList.length > 0">
          <el-button class="action-btn clear-btn" size="small" plain @click.stop="handleClearAllSuccess">
            <el-icon><Delete /></el-icon>
            <span>全部清除</span>
          </el-button>
        </template>
        <!-- 上传失败的批量操作 -->
        <template v-if="activeName === 'uploadFail' && uploaderStore.uploadFailFileList.length > 0">
          <el-button class="action-btn restart-btn" size="small" plain @click.stop="handleRestartAllFailed">
            <el-icon><RefreshRight /></el-icon>
            <span>全部重新开始</span>
          </el-button>
        </template>
      </div>
    </div>
    <el-tabs tab-position="left" v-model="activeName">
      <el-tab-pane :label="uploadingLabel" name="uploading">
        <UploadFileList :fileList="uploaderStore.uploadingFileList" />
      </el-tab-pane>
      <el-tab-pane :label="uploadSuccessLabel" name="uploadSuccess">
        <UploadFileList :fileList="uploaderStore.uploadSuccessFileList" />
      </el-tab-pane>
      <el-tab-pane :label="uploadFailLabel" name="uploadFail">
        <UploadFileList :fileList="uploaderStore.uploadFailFileList" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { VideoPlay, VideoPause, Close, Delete, RefreshRight } from '@element-plus/icons-vue'
import UploadFileList from '@/views/netdisk/components/Uploader/UploadFileList.vue'
import { useUploaderStore } from '@/stores/uploader'

const uploaderStore = useUploaderStore()

const activeName = ref('uploading')
const uploadingLabel = computed(() => `上传中(${uploaderStore.uploadingFileList.length})`)
const uploadSuccessLabel = computed(() => `上传完成(${uploaderStore.uploadSuccessFileList.length})`)
const uploadFailLabel = computed(() => `上传失败(${uploaderStore.uploadFailFileList.length})`)

/**
 * 是否有暂停的文件
 */
const hasPausedFiles = computed(() => {
  return uploaderStore.uploadingFileList.some(file => file.status === 'pause')
})

/**
 * 全部开始上传
 */
const handleStartAll = () => {
  uploaderStore.startAllUpload()
}

/**
 * 全部暂停上传
 */
const handlePauseAll = () => {
  uploaderStore.pauseAllUpload()
}

/**
 * 全部取消上传（只取消暂停状态的文件）
 */
const handleCancelAll = () => {
  uploaderStore.cancelAllUpload()
}

/**
 * 清除所有上传成功记录
 */
const handleClearAllSuccess = () => {
  uploaderStore.clearAllSuccessRecord()
}

/**
 * 全部重新开始失败的上传
 */
const handleRestartAllFailed = () => {
  uploaderStore.restartAllFailedUpload()
}
</script>

<style scoped lang="scss">
.uploader-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .uploader-title {
    border-bottom: 1px solid #ddd;
    line-height: 40px;
    padding: 0 10px;
    font-size: 15px;
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title-left {
      display: flex;
      align-items: center;
      gap: 5px;
    }

    .tips {
      font-size: 13px;
      color: rgb(169, 169, 169);
    }

    .batch-operations {
      display: flex;
      gap: 8px;
      
      .action-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 5px 12px;
        font-size: 13px;
        border-radius: 6px;
        font-weight: 500;
        transition: all 0.3s;
        
        .el-icon {
          font-size: 14px;
        }
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
      
      .start-btn {
        color: #67c23a;
        border-color: #67c23a;
        background-color: #f0f9ff;
        
        &:hover {
          color: #fff;
          background-color: #67c23a;
          border-color: #67c23a;
        }
      }
      
      .pause-btn {
        color: #e6a23c;
        border-color: #e6a23c;
        background-color: #fdf6ec;
        
        &:hover {
          color: #fff;
          background-color: #e6a23c;
          border-color: #e6a23c;
        }
      }
      
      .cancel-btn {
        color: #f56c6c;
        border-color: #f56c6c;
        background-color: #fef0f0;
        
        &:hover {
          color: #fff;
          background-color: #f56c6c;
          border-color: #f56c6c;
        }
      }
      
      .clear-btn {
        color: #f56c6c;
        border-color: #f56c6c;
        background-color: #fef0f0;
        
        &:hover {
          color: #fff;
          background-color: #f56c6c;
          border-color: #f56c6c;
        }
      }
      
      .restart-btn {
        color: #409eff;
        border-color: #409eff;
        background-color: #ecf5ff;
        
        &:hover {
          color: #fff;
          background-color: #409eff;
          border-color: #409eff;
        }
      }
    }
  }

  // 修复左侧菜单栏竖线延伸到底部
  :deep(.el-tabs--left) {
    flex: 1;
    display: flex;
    overflow: hidden;
    
    .el-tabs__header {
      height: 100%;
      
      .el-tabs__nav-wrap {
        height: 100%;
        
        &::after {
          height: 100%;
        }
      }
    }
    
    .el-tabs__content {
      flex: 1;
      overflow: hidden;
    }
  }

}
</style>