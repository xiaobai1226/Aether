import httpInstance from '@/utils/http'
import httpArrayBufferInstance from '@/utils/HttpArrayBuffer'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'
import type { AxiosPromise, AxiosProgressEvent } from 'axios'
import type {
  CopyRequest, DeleteRequest,
  FileRenameRequest,
  GetFileListByPageRequest,
  GetFileListByPageResponse, GetFolderListByPageRequest, MoveRequest,
  NewFolderRequest, UploadFileRequest, UploadFileResponse
} from '@/api/v1/file/types'
import { useAccountStore } from '@/stores/account'
import { ApiVersion } from '@/api/ApiVersion'

const baseUrl = ApiVersion.API_V1 + '/file'

/**
 * 分页获取文件列表
 * @param params
 * @param loadingTarget
 * @param showLoading
 */
export const getFileListByPage = (params: GetFileListByPageRequest, showLoading: Boolean, loadingTarget?: HTMLElement): AxiosPromise<GetFileListByPageResponse> => {
  const url = baseUrl + '/getFileListByPage'
  return httpInstance.get(url, {
    params: params,
    showSuccessMsg: false,
    showLoading: showLoading,
    loadingTarget: loadingTarget
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 分页获取文件夹列表
 * @param params
 * @param loadingTarget
 */
export const getFolderListByPage = (params: GetFolderListByPageRequest, loadingTarget?: HTMLElement): AxiosPromise<GetFileListByPageResponse> => {
  const url = baseUrl + '/getFolderListByPage'
  return httpInstance.get(url, {
    params: params,
    showSuccessMsg: false,
    loadingTarget: loadingTarget
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 新建文件夹
 * @param data
 */
export const newFolder = (data: NewFolderRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/newFolder',
    method: 'POST',
    data: data
  })
}

/**
 * 重命名
 * @param data
 */
export const rename = (data: FileRenameRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/rename',
    method: 'POST',
    data: data
  })
}

/**
 * 上传文件
 * @param uploadFileRequest 请求参数
 * @param chunkFile 上传文件
 * @param onUploadProgress 上传进度回调
 */
export const uploadFile = (uploadFileRequest: UploadFileRequest, chunkFile: Blob, onUploadProgress: (progressEvent: AxiosProgressEvent) => void): AxiosPromise<UploadFileResponse> => {
  const formData = new FormData()
  formData.append('file', chunkFile)
  Object.entries(uploadFileRequest).forEach(([key, value]) => {
    formData.append(key, value)
  })
  // formData.append('uploadFileVO', JSON.stringify(uploadFileRequest));

  const url = baseUrl + '/uploadFile'
  return httpInstance.post(url, formData, {
    onUploadProgress: onUploadProgress,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    showErrMsg: false,
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 取消文件上传
 * @param taskId 任务ID
 */
export const cancelUploadFile = (taskId: string): AxiosPromise => {
  const url = baseUrl + '/cancelUploadFile'
  const data = { taskId: taskId }
  return httpInstance.post(url, data, {
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 移动文件
 * @param data
 */
export const move = (data: MoveRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/move',
    method: 'POST',
    data: data
  })
}

/**
 * 复制文件
 * @param data
 */
export const copy = (data: CopyRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/copy',
    method: 'POST',
    data: data
  })
}

/**
 * 删除文件
 * @param data 请求数据
 */
export const del = (data: DeleteRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/delete',
    method: 'POST',
    data: data
  })
}

/**
 * 获取缩略图
 */
export const getThumbnail = (thumbnail: string): AxiosPromise<ArrayBuffer> => {
  return httpArrayBufferInstance({
    url: baseUrl + '/getThumbnail',
    params: { thumbnail: thumbnail },
    method: 'GET',
    responseType: 'arraybuffer'
  })
}

/**
 * 获取缩略图Url
 */
export const getThumbnailUrl = (thumbnail: string): string => {
  // 从pinia获取token数据
  const accountStore = useAccountStore()
  const { tokenName, tokenPrefix, token } = accountStore.accountInfo
  // 按照后端要求拼接token数据
  const tokenString = tokenName + ':' + tokenPrefix + ' ' + token
  const sign = btoa(tokenString)

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getThumbnail?thumbnail=' + thumbnail + '&sign=' + sign
}

/**
 * 获取图片
 */
export const getImage = (id: number): AxiosPromise<ArrayBuffer> => {
  return httpArrayBufferInstance({
    url: baseUrl + '/getImage',
    params: { id: id },
    method: 'GET',
    responseType: 'arraybuffer'
  })
}

/**
 * 获取图片Url
 */
export const getImageUrl = (id: number): string => {
  // 从pinia获取token数据
  const accountStore = useAccountStore()
  const { tokenName, tokenPrefix, token } = accountStore.accountInfo
  // 按照后端要求拼接token数据
  const tokenString = tokenName + ':' + tokenPrefix + ' ' + token
  const sign = btoa(tokenString)

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getImage?id=' + id + '&sign=' + sign
}

/**
 * 获取视频
 */
export const getVideo = (id: number): AxiosPromise<ArrayBuffer> => {
  return httpArrayBufferInstance({
    url: baseUrl + '/getVideo',
    params: { id: id },
    method: 'GET',
    responseType: 'arraybuffer'
  })
}

/**
 * 获取视频Url
 */
export const getVideoUrl = (id: number): string => {
  // 从pinia获取token数据
  const accountStore = useAccountStore()
  const { tokenName, tokenPrefix, token } = accountStore.accountInfo
  // 按照后端要求拼接token数据
  const tokenString = tokenName + ':' + tokenPrefix + ' ' + token
  const sign = btoa(tokenString)

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getVideo?id=' + id + '&sign=' + sign
}

/**
 * 获取文件
 */
export const getFile = (id: number): AxiosPromise<ArrayBuffer> => {
  return httpArrayBufferInstance({
    url: baseUrl + '/getFile',
    params: { id: id },
    method: 'GET',
    responseType: 'arraybuffer'
  })
}

/**
 * 获取文件Url
 */
export const getFileUrl = (id: number): string => {
  // 从pinia获取token数据
  const accountStore = useAccountStore()
  const { tokenName, tokenPrefix, token } = accountStore.accountInfo
  // 按照后端要求拼接token数据
  const tokenString = tokenName + ':' + tokenPrefix + ' ' + token
  const sign = btoa(tokenString)

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getFile?id=' + id + '&sign=' + sign
}

/**
 * 获取下载链接
 * @param ids
 */
export const createDownloadSign = (ids: string): AxiosPromise => {
  const url = baseUrl + '/createDownloadSign'
  const data = { ids: ids }

  return httpInstance.post(url, data, {
    showSuccessMsg: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 获取下载URL
 */
export const getDownloadUrl = (sign: string): string => {
  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/download?sign=' + sign
}