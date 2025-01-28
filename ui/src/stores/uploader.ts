/**
 * 管理用户相关数据
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UploadFileItem } from '@/views/netdisk/components/Uploader/types'
import { STATUS } from '@/views/netdisk/components/Uploader/types'
import SparkMD5 from 'spark-md5'
import type { UploadFileRequest } from '@/api/v1/file/types'
import { uploadFile, cancelUploadFile } from '@/api/v1/file'
import { useUserStore } from '@/stores/user'

export const useUploaderStore = defineStore('uploader', () => {

  // 从pinia获取用户数据
  const userStore = useUserStore()

  // 上传中文件列表
  const uploadingFileList = ref<Array<UploadFileItem>>([])

  // 上传成功文件列表
  const uploadSuccessFileList = ref<Array<UploadFileItem>>([])

  // 上传失败文件列表
  const uploadFailFileList = ref<Array<UploadFileItem>>([])

  /**
   * 是否展示上传页面状态，默认为false
   */
  const isShowUploader = ref(false)

  /**
   * 修改是否展示上传页面状态
   */
  const updateShowUploader = (value: boolean) => {
    isShowUploader.value = value
  }

  /**
   * 上传中数量
   */
  const uploadingNum = ref<number>(0)

  /**
   * 分片大小
   */
  const chunkSize = 1024 * 1024 * 5

  // TODO 确定delList的类型
  const delList = ref<string[]>([])

  /**
   * Uploader的增加上传文件的方法
   */
  const addUploadFile = async (file: File, uid: string, path: string | null, uploadedCallback: (uploadPath?: string) => void) => {
    isShowUploader.value = true

    const fileItem: UploadFileItem = {
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
    uploadingFileList.value.push(fileItem)
    // if (fileItem.totalSize == 0) {
    //   fileItem.status = STATUS.empty_file.value
    //   return
    // }

    // 最大同时上传数量 3
    if (uploadingNum.value >= 3) {
      fileItem.status = STATUS.wait.value
    } else {
      uploadingNum.value++

      // 上传文件
      await md5AndUploadFile(uid, 0, uploadedCallback)
    }
  }

  /**
   * 获取上传中文件
   * @param uid
   */
  const getUploadingFileByUid = (uid: string): UploadFileItem | undefined => {
    const fileItem = uploadingFileList.value.find((item => {
      return item.uid === uid
    }))
    return fileItem
  }

  /**
   * 获取上传成功文件
   * @param uid
   */
  const getUploadSuccessFileByUid = (uid: string): UploadFileItem | undefined => {
    const fileItem = uploadSuccessFileList.value.find((item => {
      return item.uid === uid
    }))
    return fileItem
  }

  /**
   * 获取上传失败文件
   * @param uid
   */
  const getUploadFailFileByUid = (uid: string): UploadFileItem | undefined => {
    const fileItem = uploadFailFileList.value.find((item => {
      return item.uid === uid
    }))
    return fileItem
  }

  /**
   * MD5并上传文件
   */
  const md5AndUploadFile = async (uid: string, chunkIndex: number, uploadedCallback: (uploadPath?: string) => void) => {

    const file = getUploadingFileByUid(uid)

    if (!file) {
      return
    }

    if (chunkIndex == 0) {
      file.status = STATUS.init.value
      // 计算MD5
      const md5FileUid = await computeMD5(file)
      if (md5FileUid == null) {
        return
      }
    }

    // 上传文件
    await handleUploadFile(uid, chunkIndex, uploadedCallback)
  }

  /**
   * 计算MD5
   * @param fileItem
   */
  const computeMD5 = (fileItem: UploadFileItem) => {
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
      const resultFile = getUploadingFileByUid(fileItem.uid)

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
   * 上传文件
   * @param uid
   * @param chunkIndex
   * @param uploadedCallback
   */
  const handleUploadFile = async (uid: string, chunkIndex: number, uploadedCallback: (uploadPath?: string) => void) => {
    chunkIndex = chunkIndex ? chunkIndex : 0
    // 分片上传
    const currentFile = getUploadingFileByUid(uid)
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

        const delIndex = delList.value.indexOf(uid)
        if (delIndex != -1) {
          delList.value.splice(delIndex, 1)
          break
        }

        const currentUploadFile = getUploadingFileByUid(uid)

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
            const uploadProgress = Math.floor((currentUploadFile.uploadedSize / fileSize) * 100)
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

              // 将数据写入上传成功列表
              uploadSuccessFileList.value.push(currentUploadFile)

              // 从上传列表中去除
              const index: number = uploadingFileList.value.findIndex((uploadFile) => uploadFile.uid === currentUploadFile.uid)
              if (index !== -1) {
                clearUploadRecord(currentUploadFile.uid, index, 1)
              }

              shouldBreak = true
              // 执行回调
              uploadedCallback(uploadFileRequest.path)
              // 更新用户空间
              userStore.handleGetUserSpaceUsage()
            }

            if (data.status === 0 || data.status === 2 || data.status === 3 || (data.status === 1 && currentUploadFile.status === STATUS.pause.value)) {
              uploadingNum.value--
              startOneWaitingUpload()
            }
          }).catch((error) => {
            currentUploadFile.errorMsg = error.response.data.msg
            currentUploadFile.status = STATUS.fail.value

            // 将数据写入上传失败列表
            uploadFailFileList.value.push(currentUploadFile)

            // 从上传列表中去除
            const index: number = uploadingFileList.value.findIndex((uploadFile) => uploadFile.uid === currentUploadFile.uid)
            if (index !== -1) {
              clearUploadRecord(currentUploadFile.uid, index, 1)
            }

            shouldBreak = true

            uploadingNum.value--
            startOneWaitingUpload()
          })
        }
      }
    }
  }

  /**
   * 获取第一个等待中上传文件
   */
  const getFirstWaitingFile = () => {
    const fileItem = uploadingFileList.value.find((item => item.status === STATUS.wait.value))
    return fileItem
  }

  /**
   * 开始一个等待中上传
   */
  const startOneWaitingUpload = () => {
    const file = getFirstWaitingFile()
    if (file && uploadingNum.value < 3) {
      uploadingNum.value++

      file.status = STATUS.uploading.value
      md5AndUploadFile(file.uid, file.currentChunkIndex, file.uploadedCallback)
    }
  }

  /**
   * 开始上传
   * @param uid 文件ID
   * @param type 操作类型 1 暂停状态重新开始 2 取消状态重新开始
   */
  const startUpload = (uid: string, type: number) => {
    let file: UploadFileItem | undefined
    if (type === 1) {
      file = getUploadingFileByUid(uid)
    } else if (type === 2) {
      file = getUploadFailFileByUid(uid)
    }

    if (file && (file.status === STATUS.pause.value || file.status === STATUS.cancel.value)) {
      if (file.status === STATUS.cancel.value) {
        // 将数据写入上传中列表
        uploadingFileList.value.push(file)

        // 从上传失败列表中去除
        const index: number = uploadFailFileList.value.findIndex((uploadFile) => file && (uploadFile.uid === file.uid))
        if (index !== -1) {
          clearUploadRecord(file.uid, index, 3)
        }
      }

      if (uploadingNum.value >= 3) {
        file.status = STATUS.wait.value
      } else {
        uploadingNum.value++
        file.status = STATUS.uploading.value
        md5AndUploadFile(uid, file.currentChunkIndex, file.uploadedCallback)
      }
    }
  }

  /**
   * 暂停上传
   */
  const pauseUpload = (uid: string) => {
    const file = getUploadingFileByUid(uid)
    if (file && file.status === STATUS.uploading.value) {
      file.status = STATUS.pause.value
    }
  }

  /**
   * 取消上传
   */
  const cancelUpload = (uid: string) => {
    const file = getUploadingFileByUid(uid)
    if (file && file.status === STATUS.pause.value) {
      cancelUploadFile(file.taskId).then(() => {
        file.status = STATUS.cancel.value
        file.currentChunkIndex = 0
        file.uploadProgress = 0
        file.uploadedSize = 0

        // 将数据写入上传失败列表
        uploadFailFileList.value.push(file)

        // 从上传列表中去除
        const index: number = uploadingFileList.value.findIndex((uploadFile) => uploadFile.uid === file.uid)
        if (index !== -1) {
          clearUploadRecord(file.uid, index, 1)
        }
      })
    }
  }

  /**
   * 清除上传记录
   * @param uid 文件ID
   * @param index 索引位置
   * @param type 操作类型 1 上传中 2 上传成功 3 上传失败
   */
  const clearUploadRecord = (uid: string, index: number, type: number) => {
    if (type === 1) {
      const file = getUploadingFileByUid(uid)
      file && uploadingFileList.value.splice(index, 1)
    } else if (type === 2) {
      const file = getUploadSuccessFileByUid(uid)
      file && uploadSuccessFileList.value.splice(index, 1)
    } else if (type === 3) {
      const file = getUploadFailFileByUid(uid)
      file && uploadFailFileList.value.splice(index, 1)
    }
  }

  // 以对象的格式把state和action返回
  return {
    isShowUploader,
    updateShowUploader,
    addUploadFile,
    uploadingFileList,
    uploadSuccessFileList,
    uploadFailFileList,
    pauseUpload,
    startUpload,
    cancelUpload,
    clearUploadRecord
  }
})