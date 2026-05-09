/**
 * 上传任务状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import SparkMD5 from 'spark-md5'
import type { DownloadTaskItem, UploadFileItem } from '@/views/netdisk/components/Uploader/types'
import { DOWNLOAD_STATUS, STATUS } from '@/views/netdisk/components/Uploader/types'
import type {
  UploadChunkRequest,
  UploadCompleteRequest,
  UploadInitRequest
} from '@/api/v1/file/types'
import {
  createDownload,
  getDownloadTask,
  getDownloadTaskFileUrl,
  getDownloadUrl,
  uploadCancel,
  uploadChunk,
  uploadComplete,
  uploadInit
} from '@/api/v1/file'
import { useUserStore } from '@/stores/user'

const MAX_CONCURRENT = 3
const MAX_RETRY = 3
const RETRY_BASE_DELAY = 1000
const CHUNK_SIZE = 1024 * 1024 * 5

export const useUploaderStore = defineStore('uploader', () => {
  const userStore = useUserStore()

  const uploadingFileList = ref<Array<UploadFileItem>>([])
  const uploadSuccessFileList = ref<Array<UploadFileItem>>([])
  const uploadFailFileList = ref<Array<UploadFileItem>>([])
  const downloadingTaskList = ref<Array<DownloadTaskItem>>([])
  const downloadSuccessTaskList = ref<Array<DownloadTaskItem>>([])
  const downloadFailTaskList = ref<Array<DownloadTaskItem>>([])
  const isShowUploader = ref(false)

  const runningCount = ref(0)
  const runningSet = new Set<string>()
  const abortControllerMap = new Map<string, AbortController>()

  const updateShowUploader = (value: boolean) => {
    isShowUploader.value = value
  }

  const getUploadingFileByUid = (uid: string): UploadFileItem | undefined =>
    uploadingFileList.value.find(item => item.uid === uid)

  const getUploadSuccessFileByUid = (uid: string): UploadFileItem | undefined =>
    uploadSuccessFileList.value.find(item => item.uid === uid)

  const getUploadFailFileByUid = (uid: string): UploadFileItem | undefined =>
    uploadFailFileList.value.find(item => item.uid === uid)

  const getDownloadingTaskById = (taskId: string): DownloadTaskItem | undefined =>
    downloadingTaskList.value.find(item => item.taskId === taskId)

  const getDownloadFailTaskById = (taskId: string): DownloadTaskItem | undefined =>
    downloadFailTaskList.value.find(item => item.taskId === taskId)

  const updateDownloadingTask = (taskId: string, updater: (current: DownloadTaskItem) => DownloadTaskItem) => {
    const index = downloadingTaskList.value.findIndex(item => item.taskId === taskId)
    if (index === -1) {
      return
    }
    const current = downloadingTaskList.value[index]
    downloadingTaskList.value[index] = updater({ ...current })
  }

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

  const clearDownloadRecord = (taskId: string, index: number, type: number) => {
    if (type === 1) {
      const task = getDownloadingTaskById(taskId)
      task && downloadingTaskList.value.splice(index, 1)
    } else if (type === 2) {
      const task = downloadSuccessTaskList.value.find(item => item.taskId === taskId)
      task && downloadSuccessTaskList.value.splice(index, 1)
    } else if (type === 3) {
      const task = getDownloadFailTaskById(taskId)
      task && downloadFailTaskList.value.splice(index, 1)
    }
  }

  const sleep = async (ms: number) => {
    await new Promise(resolve => setTimeout(resolve, ms))
  }

  const calcChunkCount = (size: number): number => {
    const chunks = Math.ceil(size / CHUNK_SIZE)
    return chunks === 0 ? 1 : chunks
  }

  const normalizeErrorMsg = (error: any, fallback = '上传失败'): string => {
    if (error?.response?.data?.msg) {
      return error.response.data.msg
    }
    if (error?.message) {
      return error.message
    }
    return fallback
  }

  const updateProgress = (fileItem: UploadFileItem, uploadedSize: number) => {
    const safeUploadedSize = Math.min(uploadedSize, fileItem.totalSize)
    fileItem.uploadedSize = safeUploadedSize
    fileItem.uploadProgress = Math.floor((safeUploadedSize / fileItem.totalSize) * 100) || 0

    const currentTime = Date.now()
    const timeDiff = (currentTime - (fileItem.lastUpdateTime || currentTime)) / 1000
    if (timeDiff > 0.5) {
      const sizeDiff = safeUploadedSize - (fileItem.lastUploadedSize || 0)
      const speed = Math.floor(sizeDiff / timeDiff)
      fileItem.uploadSpeed = speed > 0 ? speed : 0
      if (speed > 0) {
        const remainingSize = fileItem.totalSize - safeUploadedSize
        fileItem.remainingTime = Math.ceil(remainingSize / speed)
      }
      fileItem.lastUpdateTime = currentTime
      fileItem.lastUploadedSize = safeUploadedSize
    }
  }

  const releaseRunning = (uid: string) => {
    if (runningSet.has(uid)) {
      runningSet.delete(uid)
      runningCount.value = Math.max(0, runningCount.value - 1)
    }
  }

  const moveToFailList = (fileItem: UploadFileItem, status: string, errorMsg?: string) => {
    fileItem.status = status
    fileItem.errorMsg = errorMsg || fileItem.errorMsg || '上传失败'
    releaseRunning(fileItem.uid)
    abortControllerMap.delete(fileItem.uid)

    const index = uploadingFileList.value.findIndex(item => item.uid === fileItem.uid)
    if (index !== -1) {
      uploadingFileList.value.splice(index, 1)
    }

    if (!uploadFailFileList.value.find(item => item.uid === fileItem.uid)) {
      uploadFailFileList.value.push(fileItem)
    }
  }

  const moveToSuccessList = (fileItem: UploadFileItem) => {
    fileItem.status = STATUS.upload_finish.value
    fileItem.uploadProgress = 100
    fileItem.uploadedSize = fileItem.totalSize
    fileItem.finishTime = new Date().toLocaleString()
    fileItem.uploadSpeed = 0
    fileItem.remainingTime = 0

    releaseRunning(fileItem.uid)
    abortControllerMap.delete(fileItem.uid)

    const index = uploadingFileList.value.findIndex(item => item.uid === fileItem.uid)
    if (index !== -1) {
      uploadingFileList.value.splice(index, 1)
    }

    uploadSuccessFileList.value.unshift(fileItem)
  }

  const computeMD5 = (fileItem: UploadFileItem) => {
    const file = fileItem.file
    const blobSlice = File.prototype.slice || (File as any).prototype.mozSlice || (File as any).prototype.webkitSlice
    const chunks = calcChunkCount(file.size)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()

    const loadNext = () => {
      const start = currentChunk * CHUNK_SIZE
      const end = start + CHUNK_SIZE >= file.size ? file.size : start + CHUNK_SIZE
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }

    loadNext()

    return new Promise<string | null>((resolve) => {
      const resultFile = getUploadingFileByUid(fileItem.uid)
      if (!resultFile) {
        resolve(null)
        return
      }

      fileReader.onload = (progressEvent: ProgressEvent<FileReader>) => {
        const target = progressEvent.target as FileReader
        spark.append(target.result)
        currentChunk++
        if (currentChunk < chunks) {
          resultFile.md5Progress = Math.ceil((currentChunk / chunks) * 100)
          loadNext()
        } else {
          const md5 = spark.end()
          spark.destroy()
          resultFile.md5Progress = 100
          resultFile.md5 = md5
          resolve(md5)
        }
      }

      fileReader.onerror = () => {
        resultFile.md5Progress = -1
        resolve(null)
      }
    }).catch(() => Promise.resolve(null))
  }

  const uploadOneChunkWithRetry = async (fileItem: UploadFileItem, chunkIndex: number, totalChunks: number): Promise<void> => {
    let attempt = 0
    while (attempt <= MAX_RETRY) {
      const currentUploadFile = getUploadingFileByUid(fileItem.uid)
      if (!currentUploadFile) {
        throw new Error('文件不存在')
      }

      if (currentUploadFile.status !== STATUS.uploading.value) {
        throw new Error('上传已暂停或取消')
      }

      const start = chunkIndex * CHUNK_SIZE
      const end = start + CHUNK_SIZE >= fileItem.totalSize ? fileItem.totalSize : start + CHUNK_SIZE
      const chunkFile = fileItem.file.slice(start, end)

      const request: UploadChunkRequest = {
        taskId: fileItem.taskId,
        fileName: fileItem.file.name,
        fileSize: fileItem.file.size,
        identifier: fileItem.md5,
        chunkIndex,
        totalChunks
      }

      const controller = new AbortController()
      abortControllerMap.set(fileItem.uid, controller)

      try {
        await uploadChunk(
          request,
          chunkFile,
          (progressEvent) => {
            const loaded = Math.min(progressEvent.loaded, chunkFile.size)
            const uploadedSize = chunkIndex * CHUNK_SIZE + loaded
            updateProgress(fileItem, uploadedSize)
          },
          controller.signal
        )
        fileItem.currentChunkIndex = chunkIndex + 1
        return
      } catch (error: any) {
        if (error?.name === 'CanceledError' || error?.code === 'ERR_CANCELED') {
          throw error
        }

        if (attempt >= MAX_RETRY) {
          throw error
        }

        const delay = RETRY_BASE_DELAY * Math.pow(2, attempt)
        await sleep(delay)
        attempt++
      }
    }
  }

  const startFileUpload = async (fileItem: UploadFileItem) => {
    if (runningSet.has(fileItem.uid)) {
      return
    }
    runningSet.add(fileItem.uid)
    runningCount.value++

    try {
      fileItem.status = STATUS.uploading.value
      fileItem.errorMsg = null

      if (!fileItem.md5) {
        fileItem.status = STATUS.init.value
        const md5 = await computeMD5(fileItem)
        if (!md5) {
          moveToFailList(fileItem, STATUS.fail.value, 'MD5计算失败')
          return
        }
        fileItem.status = STATUS.uploading.value
      }

      const totalChunks = calcChunkCount(fileItem.totalSize)
      const initRequest: UploadInitRequest = {
        taskId: fileItem.taskId || undefined,
        path: fileItem.path || undefined,
        relativePath: fileItem.file.webkitRelativePath || undefined,
        fileName: fileItem.file.name,
        fileSize: fileItem.file.size,
        identifier: fileItem.md5,
        totalChunks
      }

      const initResponse = await uploadInit(initRequest)
      const initData = initResponse.data
      fileItem.taskId = initData.taskId

      if (initData.status === 0) {
        fileItem.status = STATUS.upload_seconds.value
        fileItem.uploadProgress = 100
        fileItem.uploadedSize = fileItem.totalSize
        fileItem.finishTime = new Date().toLocaleString()
        releaseRunning(fileItem.uid)
        const index = uploadingFileList.value.findIndex(item => item.uid === fileItem.uid)
        if (index !== -1) {
          uploadingFileList.value.splice(index, 1)
        }
        uploadSuccessFileList.value.unshift(fileItem)
        fileItem.uploadedCallback(fileItem.path || undefined)
        userStore.handleGetUserSpaceUsage()
        return
      }

      const uploadedChunks = initData.uploadedChunks || []
      const maxUploadedChunk = uploadedChunks.length > 0 ? Math.max(...uploadedChunks) : -1
      fileItem.currentChunkIndex = maxUploadedChunk + 1
      updateProgress(fileItem, initData.uploadedSize || 0)

      for (let i = fileItem.currentChunkIndex; i < totalChunks; i++) {
        if (fileItem.status !== STATUS.uploading.value) {
          throw new Error('上传已暂停或取消')
        }
        await uploadOneChunkWithRetry(fileItem, i, totalChunks)
      }

      const completeRequest: UploadCompleteRequest = {
        taskId: fileItem.taskId,
        path: fileItem.path || undefined,
        fileName: fileItem.file.name,
        fileSize: fileItem.file.size,
        identifier: fileItem.md5,
        totalChunks
      }
      const completeResponse = await uploadComplete(completeRequest)
      if (completeResponse.data.status === 2) {
        moveToSuccessList(fileItem)
        fileItem.uploadedCallback(fileItem.path || undefined)
        userStore.handleGetUserSpaceUsage()
      } else {
        moveToFailList(fileItem, STATUS.fail.value, '上传完成确认失败')
      }
    } catch (error: any) {
      if (fileItem.status === STATUS.pause.value) {
        releaseRunning(fileItem.uid)
        abortControllerMap.delete(fileItem.uid)
      } else if (fileItem.status === STATUS.cancel.value) {
        moveToFailList(fileItem, STATUS.cancel.value, '已取消')
      } else {
        moveToFailList(fileItem, STATUS.fail.value, normalizeErrorMsg(error))
      }
    } finally {
      scheduleUpload()
    }
  }

  const scheduleUpload = () => {
    while (runningCount.value < MAX_CONCURRENT) {
      const waitingFile = uploadingFileList.value.find(item => item.status === STATUS.wait.value)
      if (!waitingFile) {
        break
      }
      startFileUpload(waitingFile)
    }
  }

  const addUploadFile = async (file: File, uid: string, path: string | null, uploadedCallback: (uploadPath?: string) => void) => {
    isShowUploader.value = true

    const fileItem: UploadFileItem = {
      file,
      uid,
      md5Progress: 0,
      md5: '',
      fileName: file.webkitRelativePath ? file.webkitRelativePath : file.name,
      status: STATUS.wait.value,
      uploadedSize: 0,
      totalSize: file.size,
      uploadProgress: 0,
      currentChunkIndex: 0,
      path,
      errorMsg: null,
      taskId: '',
      uploadedCallback,
      uploadSpeed: 0,
      remainingTime: 0,
      lastUpdateTime: Date.now(),
      lastUploadedSize: 0
    }

    uploadingFileList.value.push(fileItem)
    scheduleUpload()
  }

  const pauseUpload = (uid: string) => {
    const file = getUploadingFileByUid(uid)
    if (!file) {
      return
    }
    if (file.status === STATUS.uploading.value || file.status === STATUS.wait.value) {
      file.status = STATUS.pause.value
      const controller = abortControllerMap.get(uid)
      controller?.abort()
    }
  }

  const cancelUpload = (uid: string) => {
    const file = getUploadingFileByUid(uid)
    if (!file) {
      return
    }

    file.status = STATUS.cancel.value
    const controller = abortControllerMap.get(uid)
    controller?.abort()

    if (file.taskId) {
      uploadCancel({ taskId: file.taskId }).catch(() => {
        // 取消接口失败时，仅记录本地取消状态
      })
    }

    moveToFailList(file, STATUS.cancel.value, '已取消')
    scheduleUpload()
  }

  const startUpload = (uid: string, type: number) => {
    let file: UploadFileItem | undefined
    if (type === 1) {
      file = getUploadingFileByUid(uid)
    } else if (type === 2) {
      file = getUploadFailFileByUid(uid)
    }

    if (!file) {
      return
    }

    if (type === 2) {
      file.currentChunkIndex = 0
      file.uploadProgress = 0
      file.uploadedSize = 0
      file.uploadSpeed = 0
      file.remainingTime = 0
      file.lastUpdateTime = Date.now()
      file.lastUploadedSize = 0
      file.taskId = ''
      file.errorMsg = null

      const failIndex = uploadFailFileList.value.findIndex(item => item.uid === file?.uid)
      if (failIndex !== -1) {
        uploadFailFileList.value.splice(failIndex, 1)
      }
      uploadingFileList.value.push(file)
    }

    if (file.status === STATUS.pause.value || file.status === STATUS.fail.value || file.status === STATUS.cancel.value || file.status === STATUS.wait.value) {
      file.status = STATUS.wait.value
      scheduleUpload()
    }
  }

  const startAllUpload = () => {
    uploadingFileList.value.forEach(file => {
      if (file.status === STATUS.pause.value || file.status === STATUS.wait.value) {
        file.status = STATUS.wait.value
      }
    })
    scheduleUpload()
  }

  const pauseAllUpload = () => {
    uploadingFileList.value.forEach(file => {
      if (file.status === STATUS.uploading.value || file.status === STATUS.wait.value) {
        pauseUpload(file.uid)
      }
    })
  }

  const cancelAllUpload = () => {
    const filesToCancel = [...uploadingFileList.value].filter(
      file => file.status === STATUS.pause.value || file.status === STATUS.wait.value || file.status === STATUS.uploading.value
    )
    filesToCancel.forEach(file => {
      cancelUpload(file.uid)
    })
  }

  const clearAllSuccessRecord = () => {
    uploadSuccessFileList.value = []
  }

  const restartAllFailedUpload = () => {
    const filesToRestart = [...uploadFailFileList.value].filter(
      file => file.status === STATUS.cancel.value || file.status === STATUS.fail.value
    )

    filesToRestart.forEach(file => {
      const index = uploadFailFileList.value.findIndex(f => f.uid === file.uid)
      if (index !== -1) {
        uploadFailFileList.value.splice(index, 1)
      }
      file.currentChunkIndex = 0
      file.uploadProgress = 0
      file.uploadedSize = 0
      file.uploadSpeed = 0
      file.remainingTime = 0
      file.lastUpdateTime = Date.now()
      file.lastUploadedSize = 0
      file.taskId = ''
      file.errorMsg = null
      file.status = STATUS.wait.value
      uploadingFileList.value.push(file)
    })

    scheduleUpload()
  }

  const moveDownloadTaskToSuccess = (task: DownloadTaskItem, downloadSign: string, fileName?: string) => {
    const successTask: DownloadTaskItem = {
      ...task,
      status: DOWNLOAD_STATUS.success.value,
      progress: 100,
      downloadSign,
      fileName: fileName || task.fileName,
      finishTime: new Date().toLocaleString()
    }

    const index = downloadingTaskList.value.findIndex(item => item.taskId === task.taskId)
    if (index !== -1) {
      downloadingTaskList.value.splice(index, 1)
    }
    downloadSuccessTaskList.value.unshift(successTask)
  }

  const moveDownloadTaskToFail = (task: DownloadTaskItem, errorMsg?: string) => {
    const failTask: DownloadTaskItem = {
      ...task,
      status: DOWNLOAD_STATUS.fail.value,
      errorMsg: errorMsg || '下载失败'
    }
    const index = downloadingTaskList.value.findIndex(item => item.taskId === task.taskId)
    if (index !== -1) {
      downloadingTaskList.value.splice(index, 1)
    }
    if (!downloadFailTaskList.value.find(item => item.taskId === failTask.taskId)) {
      downloadFailTaskList.value.unshift(failTask)
    }
  }

  const pollDownloadTaskStatus = async (task: DownloadTaskItem) => {
    const maxPollCount = 600
    let pollCount = 0
    while (pollCount < maxPollCount) {
      pollCount++
      try {
        const { data } = await getDownloadTask(task.taskId)
        updateDownloadingTask(task.taskId, current => {
          current.progress = data.progress || 0
          current.fileName = data.fileName || current.fileName
          current.totalFileCount = data.totalFileCount || current.totalFileCount || 0
          current.completedFileCount = data.completedFileCount || current.completedFileCount || 0
          current.status = DOWNLOAD_STATUS.downloading.value
          return current
        })

        if (data.status === 2) {
          const current = getDownloadingTaskById(task.taskId)
          if (!current) {
            return
          }
          if (data.downloadSign) {
            moveDownloadTaskToSuccess(current, data.downloadSign, data.fileName)
            window.open(getDownloadTaskFileUrl(data.downloadSign))
          } else {
            moveDownloadTaskToFail(current, '下载任务签名缺失')
          }
          return
        }

        if (data.status === 3 || data.status === 4) {
          const current = getDownloadingTaskById(task.taskId)
          if (current) {
            moveDownloadTaskToFail(current, data.errorMsg || '下载任务失败')
          }
          return
        }
      } catch (e: any) {
        const current = getDownloadingTaskById(task.taskId)
        if (current) {
          moveDownloadTaskToFail(current, normalizeErrorMsg(e, '下载任务查询失败'))
        }
        return
      }

      await sleep(3000)
    }
    const current = getDownloadingTaskById(task.taskId)
    if (current) {
      moveDownloadTaskToFail(current, '下载任务超时，请重试')
    }
  }

  const addDownloadTask = async (taskId: string, ids: number[], fileName?: string) => {
    isShowUploader.value = true
    const taskItem: DownloadTaskItem = {
      taskId,
      ids,
      fileName: fileName || '打包下载.zip',
      status: DOWNLOAD_STATUS.preparing.value,
      progress: 0,
      totalFileCount: 0,
      completedFileCount: 0,
      errorMsg: null,
      downloadSign: '',
      createTime: Date.now()
    }
    downloadingTaskList.value.unshift(taskItem)
    await pollDownloadTaskStatus(taskItem)
  }

  const startDownloadByIds = async (ids: number[], fileName?: string) => {
    if (!ids || ids.length === 0) {
      return
    }
    isShowUploader.value = true
    const { data } = await createDownload(ids.join(','))
    if (data.type === 'DIRECT' && data.sign) {
      window.open(getDownloadUrl(data.sign))
      return
    }
    if (data.type === 'TASK' && data.taskId) {
      await addDownloadTask(data.taskId, ids, fileName)
    }
  }

  const clearAllDownloadSuccessRecord = () => {
    downloadSuccessTaskList.value = []
  }

  const restartFailedDownload = async (taskId: string) => {
    const task = getDownloadFailTaskById(taskId)
    if (!task || !task.ids || task.ids.length === 0) {
      return
    }
    clearDownloadRecord(taskId, downloadFailTaskList.value.findIndex(item => item.taskId === taskId), 3)
    await startDownloadByIds(task.ids, task.fileName)
  }

  const restartAllFailedDownload = async () => {
    const failedTasks = [...downloadFailTaskList.value]
    downloadFailTaskList.value = []
    for (const task of failedTasks) {
      if (task.ids && task.ids.length > 0) {
        await startDownloadByIds(task.ids, task.fileName)
      }
    }
  }

  return {
    isShowUploader,
    updateShowUploader,
    addUploadFile,
    uploadingFileList,
    uploadSuccessFileList,
    uploadFailFileList,
    downloadingTaskList,
    downloadSuccessTaskList,
    downloadFailTaskList,
    pauseUpload,
    startUpload,
    cancelUpload,
    clearUploadRecord,
    startAllUpload,
    pauseAllUpload,
    cancelAllUpload,
    clearAllSuccessRecord,
    restartAllFailedUpload,
    startDownloadByIds,
    clearDownloadRecord,
    clearAllDownloadSuccessRecord,
    restartFailedDownload,
    restartAllFailedDownload
  }
})