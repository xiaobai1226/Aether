<template>
  <div class="uploader-panel">
    <div class="uploader-title">
      <span>上传列表</span>
      <span class="tips">（仅展示本次上传任务）</span>
    </div>
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
            <Icon :width="28" class="btn-item" iconName="pause" title="暂停"
                  v-if="item.status === STATUS.uploading.value"
                  @click="pauseUpload(item.uid)"></Icon>
            <span v-if="item.status === STATUS.pause.value">
              <Icon :width="28" class="btn-item" iconName="upload"
                    v-if="item.status === STATUS.pause.value" title="上传"
                    @click="startUpload(item.uid)"></Icon>
              <Icon :width="28" class="del btn-item" iconName="delete" title="取消"
                    v-if="item.status === STATUS.pause.value"
                    @click="cancelUpload(item.uid)"></Icon>
            </span>
            <Icon :width="28" class="clean btn-item" iconName="clean" title="清除"
                  v-if="item.status == STATUS.upload_finish.value || item.status == STATUS.upload_seconds.value"
                  @click="clearUploadRecord(item.uid, index)"></Icon>
            <Icon :width="28" class="clean btn-item" iconName="upload" title="重新开始"
                  v-if="item.status === STATUS.cancel.value"
                  @click="startUpload(item.uid)"></Icon>
          </div>
        </div>
      </div>
      <div v-if="fileList.length == 0">
        <NoData msg="暂无上传任务"></NoData>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Utils from '@/utils/Utils'
import NoData from '@/components/NoData.vue'
import { useUploaderStore } from '@/stores/uploader'
import SparkMD5 from 'spark-md5'
import { uploadFile, cancelUploadFile } from '@/api/file'
import type { UploadFileRequest } from '@/api/file/types'
import Icon from '@/components/Icon.vue'
import { useUserStore } from '@/stores/user'

// 从pinia获取用户数据
const userStore = useUserStore()

const uploaderStore = useUploaderStore()

interface FileItem {
  // 文件，文件大小，文件流，文件名。。。
  file: File,
  // 文件uid
  uid: string,
  // MD5进度
  md5Progress: number,
  // MD5值
  md5: string,
  // 文件名
  fileName: string,
  // 上传状态
  status: string,
  // 已上传大小
  uploadedSize: number,
  // 文件总大小
  totalSize: number,
  // 上传进度
  uploadProgress: number,
  // 当前分片索引
  currentChunkIndex: number,
  // 所属文件夹ID
  path: string | null,
  // 错误信息
  errorMsg: string | null,
  // 任务ID
  taskId: string,
  // 回调方法
  uploadedCallback: () => void
}

// 定义STATUS对象
interface STATUS_OBJ {
  [key: string]: {
    value: string,
    desc: string,
    color: string,
    icon: string
  }
}

// 文件状态
const STATUS: STATUS_OBJ = {
  empty_file: {
    value: 'empty_file',
    desc: '文件为空',
    color: '#F75000',
    icon: 'close'
  },
  fail: {
    value: 'fail',
    desc: '上传失败',
    color: '#F75000',
    icon: 'close'
  },
  init: {
    value: 'init',
    desc: '解析中',
    color: '#e6a23c',
    icon: 'clock'
  },
  uploading: {
    value: 'uploading',
    desc: '上传中',
    color: '#409eff',
    icon: 'upload'
  },
  pause: {
    value: 'pause',
    desc: '已暂停',
    color: '#409eff',
    icon: 'upload'
  },
  upload_finish: {
    value: 'upload_finish',
    desc: '上传完成',
    color: '#67c23a',
    icon: 'ok'
  },
  upload_seconds: {
    value: 'upload_seconds',
    desc: '秒传',
    color: '#67c23a',
    icon: 'ok'
  },
  cancel: {
    value: 'cancel',
    desc: '已取消',
    color: '#F75000',
    icon: 'close'
  },
  wait: {
    value: 'wait',
    desc: '等待中',
    color: '#409eff',
    icon: 'dengdaizhong'
  }
}

// 分片大小
const chunkSize = 1024 * 1024 * 5
// 文件列表
const fileList = ref<Array<FileItem>>([])
// 上传中数量
let uploadingNum = 0

// TODO 确定delList的类型
const delList = ref<string[]>([])
const addUploadFile = async (file: File, uid: string, path: string | null, uploadedCallback: () => void) => {
  const fileItem: FileItem = {
    // 文件，文件大小，文件流，文件名。。。
    file: file,
    // 文件uid
    uid: uid,
    // MD5进度
    md5Progress: 0,
    // MD5值
    md5: '',
    // 文件名
    fileName: file.webkitRelativePath ? file.webkitRelativePath : file.name,
    // 上传状态
    status: STATUS.init.value,
    // 已上传大小
    uploadedSize: 0,
    // 文件总大小
    totalSize: file.size,
    // 上传进度
    uploadProgress: 0,
    // 当前分片索引
    currentChunkIndex: 0,
    // 所属文件夹ID
    path: path,
    // 错误信息
    errorMsg: null,
    // 任务ID
    taskId: '',
    // 回调方法
    uploadedCallback: uploadedCallback
  }

  // 向列表第一条插入元素
  fileList.value.push(fileItem)
  // if (fileItem.totalSize == 0) {
  //   fileItem.status = STATUS.empty_file.value
  //   return
  // }

  // 最大同时上传数量 3
  if (uploadingNum >= 3) {
    fileItem.status = STATUS.wait.value
  } else {
    uploadingNum++

    // 上传文件
    await md5AndUploadFile(uid, 0, uploadedCallback)
  }
}

// 初始化uploaderStore中的方法
uploaderStore.initAddUploadFileOfUploader(addUploadFile)

/**
 * 计算MD5
 * @param fileItem
 */
const computeMD5 = (fileItem: FileItem) => {
  const file = fileItem.file
  const blobSlice = File.prototype.slice || (File as any).prototype.mozSlice || (File as any).prototype.webkitSlice
  const chunks = Math.ceil(file.size / chunkSize)
  let currentChunk = 0
  const spark = new SparkMD5.ArrayBuffer()
  const fileReader = new FileReader()

  const loadNext = () => {
    const start = currentChunk * chunkSize
    const end = start + chunkSize >= file.size ? file.size : start + chunkSize
    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
  }
  loadNext()

  return new Promise((resolve, reject) => {
    const resultFile = getFileByUid(fileItem.uid)

    if (!resultFile) {
      throw new Error('File is not found')
    }

    fileReader.onload = (progressEvent: ProgressEvent<FileReader>) => {
      const target = progressEvent.target as FileReader
      spark.append(target.result)
      currentChunk++
      if (currentChunk < chunks) {
        // console.log(`第${file.name},${currentChunk}分片解析完成，开始第${currentChunk + 1}分片解析`);
        resultFile.md5Progress = Math.ceil((currentChunk / chunks) * 100)
        loadNext()
      } else {
        const md5 = spark.end()
        spark.destroy()
        resultFile.md5Progress = 100
        resultFile.status = STATUS.uploading.value
        resultFile.md5 = md5
        resolve(fileItem.uid)
      }
    }
    fileReader.onerror = () => {
      resultFile.md5Progress = -1
      resultFile.status = STATUS.fail.value
      resolve(fileItem.uid)
    }
  }).catch((error) => {
    // console.log(error);
    return Promise.resolve(null)
  })
}

/**
 * 获取文件
 * @param uid
 */
const getFileByUid = (uid: string): FileItem | undefined => {
  const fileItem = fileList.value.find((item => {
    return item.uid === uid
  }))
  return fileItem
}

/**
 * MD5并上传文件
 */
const md5AndUploadFile = async (uid: string, chunkIndex: number, uploadedCallback: () => void) => {

  const file = getFileByUid(uid)

  if (!file) {
    return
  }

  if (chunkIndex == 0) {
    file.status = STATUS.init.value
    // 计算MD5
    let md5FileUid = await computeMD5(file)
    if (md5FileUid == null) {
      return
    }
  }

  // 上传文件
  await handleUploadFile(uid, chunkIndex, uploadedCallback)
}

/**
 * 上传文件
 * @param uid
 * @param chunkIndex
 * @param uploadedCallback
 */
const handleUploadFile = async (uid: string, chunkIndex: number, uploadedCallback: () => void) => {
  chunkIndex = chunkIndex ? chunkIndex : 0
  // 分片上传
  let currentFile = getFileByUid(uid)
  if (currentFile) {
    const file = currentFile.file
    const fileSize = currentFile.totalSize
    let chunks = Math.ceil(fileSize / chunkSize)
    chunks = chunks === 0 ? 1 : chunks
    let shouldBreak = false
    for (let i = chunkIndex; i < chunks; i++) {
      if (shouldBreak) {
        break
      }

      let delIndex = delList.value.indexOf(uid)
      if (delIndex != -1) {
        delList.value.splice(delIndex, 1)
        break
      }

      const currentUploadFile = getFileByUid(uid)

      if (currentUploadFile) {
        if (currentUploadFile.status !== STATUS.uploading.value) {
          break
        }
        const start = i * chunkSize
        const end = start + chunkSize >= fileSize ? fileSize : start + chunkSize
        const chunkFile = file.slice(start, end)

        const uploadFileRequest: UploadFileRequest = {
          taskId: currentFile.taskId,
          fileName: file.name,
          fileSize: file.size,
          identifier: currentFile.md5,
          chunkIndex: i,
          totalChunks: chunks
        }

        if (currentFile.path) {
          uploadFileRequest.path = currentFile.path
        }

        if (file.webkitRelativePath) {
          uploadFileRequest.relativePath = file.webkitRelativePath
        }

        await uploadFile(uploadFileRequest, chunkFile, (progressEvent) => {
          let loaded = progressEvent.loaded
          if (loaded > fileSize) {
            loaded = fileSize
          }
          currentUploadFile.uploadedSize = i * chunkSize + loaded
          let uploadProgress = Math.floor((currentUploadFile.uploadedSize / fileSize) * 100)
          currentUploadFile.uploadProgress = uploadProgress ? uploadProgress : 0
        }).then(({ data }) => {
          currentUploadFile.taskId = data.taskId
          let statusString = STATUS.fail.value
          if (data.status === 0) {
            statusString = STATUS.upload_seconds.value
          } else if (data.status === 1) {
            if (currentUploadFile.status === STATUS.pause.value) {
              statusString = STATUS.pause.value
            } else {
              statusString = STATUS.uploading.value
            }
          } else if (data.status === 2) {
            statusString = STATUS.upload_finish.value
          } else if (data.status === 3) {
            statusString = STATUS.fail.value
          }

          currentUploadFile.status = STATUS[statusString].value
          currentUploadFile.currentChunkIndex = i
          if (statusString == STATUS.upload_seconds.value || statusString == STATUS.upload_finish.value) {
            currentUploadFile.uploadProgress = 100
            // emit("uploadCallback");
            shouldBreak = true
            // 执行回调
            uploadedCallback()
            // 更新用户空间
            userStore.handleGetUserSpaceUsage()
          }

          if (data.status === 0 || data.status === 2 || data.status === 3 || (data.status === 1 && currentUploadFile.status === STATUS.pause.value)) {
            uploadingNum--
            startOneWaitingUpload()
          }
        }).catch((error) => {
          currentUploadFile.errorMsg = error.response.data.msg
          currentUploadFile.status = STATUS.fail.value
          shouldBreak = true

          uploadingNum--
          startOneWaitingUpload()
        })

        // const updateResult = await proxy.Request({
        //   url: api.upload,
        //   showLoading: false,
        //   dataType: "file",
        //   params: {
        //     file: chunkFile,
        //     fileName: file.name,
        //     fileMd5: currentFile.md5,
        //     chunkIndex: i,
        //     chunks: chunks,
        //     fileId: currentFile.fileId,
        //     filePid: currentFile.filePid
        //   },
        //   showError: false,
        //   errorCallbak: (errorMsg) => {
        //     currentFile.errorMsg = errorMsg;
        //     currentFile.status = STATUS.fail.value;
        //   },
        //   uploadProgressCallback: (event) => {
        //     let loaded = event.loaded;
        //     if (loaded > fileSize) {
        //       loaded = fileSize;
        //     }
        //     currentFile.uploadedSize = i * chunkSize + loaded;
        //     currentFile.uploadProgress = Math.floor((currentFile.uploadedSize / fileSize) * 100);
        //   }
        // });
        // if (updateResult == null) {
        //   break;
        // }
        // currentFile.fileId = updateResult.data.fileId;
        // currentFile.status = STATUS[updateResult.data.status].value;
        // currentFile.chunkIndex = i;
        // if (updateResult.data.status == STATUS.upload_seconds.value || updateResult.data.status == STATUS.upload_finish.value) {
        //   currentFile.uploadProgress = 100;
        //   emit("uploadCallback");
        //   break;
        // }
      }
    }
  }
}

/**
 * 获取第一个等待中上传文件
 */
const getFirstWaitingFile = () => {
  const fileItem = fileList.value.find((item => item.status === STATUS.wait.value))
  return fileItem
}

/**
 * 开始一个等待中上传
 */
const startOneWaitingUpload = () => {
  const file = getFirstWaitingFile()
  if (file && uploadingNum < 3) {
    uploadingNum++

    file.status = STATUS.uploading.value
    md5AndUploadFile(file.uid, file.currentChunkIndex, file.uploadedCallback)
  }
}

/**
 * 开始上传
 */
const startUpload = (uid: string) => {
  const file = getFileByUid(uid)
  if (file && (file.status === STATUS.pause.value || file.status === STATUS.cancel.value)) {
    if (uploadingNum >= 3) {
      file.status = STATUS.wait.value
    } else {
      uploadingNum++
      file.status = STATUS.uploading.value
      md5AndUploadFile(uid, file.currentChunkIndex, file.uploadedCallback)
    }
  }
}

/**
 * 暂停上传
 */
const pauseUpload = (uid: string) => {
  const file = getFileByUid(uid)
  if (file && file.status === STATUS.uploading.value) {
    file.status = STATUS.pause.value
  }
}

/**
 * 取消上传
 */
const cancelUpload = (uid: string) => {
  const file = getFileByUid(uid)
  if (file && file.status === STATUS.pause.value) {
    cancelUploadFile(file.taskId).then(() => {
      file.status = STATUS.cancel.value
      file.currentChunkIndex = 0
      file.uploadProgress = 0
      file.uploadedSize = 0
    })
  }
}

/**
 * 清除上传记录
 */
const clearUploadRecord = (uid: string, index: number) => {
  const file = getFileByUid(uid)
  if (file) {
    fileList.value.splice(index, 1)
  }
}
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
}
</style>