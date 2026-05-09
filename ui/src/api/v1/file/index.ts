import httpInstance, { uploadHttpInstance } from '@/utils/http'
import httpArrayBufferInstance from '@/utils/HttpArrayBuffer'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'
import type { AxiosPromise, AxiosProgressEvent } from 'axios'
import type {
  CopyRequest, DeleteRequest,
  CreateDirectLinkRequest, CreateDirectLinkResponse,
  GetDirectLinkListByPageRequest, GetDirectLinkListByPageResponse,
  DownloadCreateResponse,
  DownloadTaskStatusResponse,
  FileRenameRequest,
  GetFileListByPageRequest,
  GetFileListByPageResponse, GetFolderListByPageRequest, MoveRequest,
  NewFolderRequest, RevokeDirectLinkRequest, UpdateDirectLinkExpireRequest, UploadCancelRequest, UploadChunkRequest, UploadChunkResponse, UploadCompleteRequest, UploadCompleteResponse, UploadInitRequest, UploadInitResponse, UploadStatusRequest, UploadStatusResponse
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

export const uploadInit = (data: UploadInitRequest): AxiosPromise<UploadInitResponse> => {
  return httpInstance.post(baseUrl + '/uploadInit', data, {
    showErrMsg: false,
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

export const uploadChunk = (
  uploadChunkRequest: UploadChunkRequest,
  chunkFile: Blob,
  onUploadProgress: (progressEvent: AxiosProgressEvent) => void,
  signal?: AbortSignal
): AxiosPromise<UploadChunkResponse> => {
  const formData = new FormData()
  formData.append('file', chunkFile)
  Object.entries(uploadChunkRequest).forEach(([key, value]) => {
    formData.append(key, String(value))
  })

  return uploadHttpInstance.post(baseUrl + '/uploadChunk', formData, {
    onUploadProgress,
    signal,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    showErrMsg: false,
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

export const uploadStatus = (params: UploadStatusRequest): AxiosPromise<UploadStatusResponse> => {
  return httpInstance.get(baseUrl + '/uploadStatus', {
    params,
    showErrMsg: false,
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

export const uploadComplete = (data: UploadCompleteRequest): AxiosPromise<UploadCompleteResponse> => {
  return httpInstance.post(baseUrl + '/uploadComplete', data, {
    showErrMsg: false,
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

export const uploadCancel = (data: UploadCancelRequest): AxiosPromise => {
  return httpInstance.post(baseUrl + '/uploadCancel', data, {
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

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getThumbnail?thumbnail=' + encodeURIComponent(thumbnail) + '&sign=' + encodeURIComponent(sign)
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

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getImage?id=' + id + '&sign=' + encodeURIComponent(sign)
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

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getVideo?id=' + id + '&sign=' + encodeURIComponent(sign)
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

  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/getFile?id=' + id + '&sign=' + encodeURIComponent(sign)
}

/**
 * 获取下载URL
 */
export const getDownloadUrl = (sign: string): string => {
  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/download?sign=' + sign
}

/**
 * 创建下载（自动判断直下或任务）
 * @param ids
 */
export const createDownload = (ids: string): AxiosPromise<DownloadCreateResponse> => {
  const url = baseUrl + '/createDownload'
  const data = { ids: ids }

  return httpInstance.post(url, data, {
    showSuccessMsg: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 创建文件直链
 * @param data
 */
export const createDirectLink = (data: CreateDirectLinkRequest): AxiosPromise<CreateDirectLinkResponse> => {
  const url = baseUrl + '/createDirectLink'
  return httpInstance.post(url, data, {
    showSuccessMsg: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 撤销文件直链
 * @param data
 */
export const revokeDirectLink = (data: RevokeDirectLinkRequest): AxiosPromise => {
  const url = baseUrl + '/revokeDirectLink'
  return httpInstance.post(url, data, {
    showSuccessMsg: true
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 获取文件直链URL
 */
export const getDirectLinkUrl = (token: string, type: 'file' | 'image' | 'video' = 'file'): string => {
  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/direct?token=' + encodeURIComponent(token) + '&type=' + encodeURIComponent(type)
}

/**
 * 分页获取直链记录
 * @param params
 */
export const getDirectLinkListByPage = (params: GetDirectLinkListByPageRequest): AxiosPromise<GetDirectLinkListByPageResponse> => {
  const url = baseUrl + '/getDirectLinkListByPage'
  return httpInstance.get(url, {
    params,
    showSuccessMsg: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 更新直链有效期
 * @param data
 */
export const updateDirectLinkExpire = (data: UpdateDirectLinkExpireRequest): AxiosPromise<CreateDirectLinkResponse> => {
  const url = baseUrl + '/updateDirectLinkExpire'
  return httpInstance.post(url, data, {
    showSuccessMsg: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 获取下载任务状态
 * @param taskId
 */
export const getDownloadTask = (taskId: string): AxiosPromise<DownloadTaskStatusResponse> => {
  const url = baseUrl + '/getDownloadTask'
  return httpInstance.get(url, {
    params: { taskId },
    showSuccessMsg: false,
    showLoading: false
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 获取下载任务文件URL
 */
export const getDownloadTaskFileUrl = (sign: string): string => {
  return import.meta.env.VITE_HTTP_BASE_URL + baseUrl + '/downloadTaskFile?sign=' + sign
}