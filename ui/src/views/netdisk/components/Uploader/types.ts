/**
 * 上传文件信息
 */
export interface UploadFileItem {
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
  uploadedCallback: (uploadPath?: string) => void
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
export const STATUS: STATUS_OBJ = {
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