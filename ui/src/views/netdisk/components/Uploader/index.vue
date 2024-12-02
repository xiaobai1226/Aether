<template>
  <div class="uploader-panel">
    <div class="uploader-title">
      <span>上传列表</span>
      <span class="tips">（仅展示本次上传任务）</span>
    </div>
    <el-tabs tab-position="left" v-model="activeName">
      <el-tab-pane :label="uploadingLabel" name="uploading">
        <UploadFileList :fileList="uploaderStore.uploadingFileList" />
      </el-tab-pane>
      <el-tab-pane :label="uploadSuccessLabel">
        <UploadFileList :fileList="uploaderStore.uploadSuccessFileList" />
      </el-tab-pane>
      <el-tab-pane :label="uploadFailLabel">
        <UploadFileList :fileList="uploaderStore.uploadFailFileList" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import UploadFileList from '@/views/netdisk/components/Uploader/UploadFileList.vue'
import { useUploaderStore } from '@/stores/uploader'

const uploaderStore = useUploaderStore()

const activeName = ref('uploading')
const uploadingLabel = computed(() => `上传中(${uploaderStore.uploadingFileList.length})`)
const uploadSuccessLabel = computed(() => `上传完成(${uploaderStore.uploadSuccessFileList.length})`)
const uploadFailLabel = computed(() => `上传失败(${uploaderStore.uploadFailFileList.length})`)
</script>

<style scoped lang="scss">
.uploader-panel {
  .uploader-title {
    border-bottom: 1px solid #ddd;
    line-height: 40px;
    padding: 0 10px;
    font-size: 15px;

    .tips {
      font-size: 13px;
      color: rgb(169, 169, 169);
    }
  }
}
</style>