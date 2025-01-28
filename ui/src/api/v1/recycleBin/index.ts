import httpInstance from '@/utils/http'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'
import type { AxiosPromise } from 'axios'
import type {
  DeleteOrRestoreRequest,
  GetRecycleBinListByPageRequest,
  GetRecycleBinListByPageResponse
} from '@/api/v1/recycleBin/types'
import { ApiVersion } from '@/api/ApiVersion'

const baseUrl = ApiVersion.API_V1 + '/recyclebin'

/**
 * 分页获取文件列表
 * @param params
 */
export const getRecycleBinListByPage = (params: GetRecycleBinListByPageRequest, showLoading: Boolean, loadingTarget?: HTMLElement): AxiosPromise<GetRecycleBinListByPageResponse> => {
  const url = baseUrl + '/getRecycleBinListByPage'
  return httpInstance.get(url, {
    params: params,
    showSuccessMsg: false,
    showLoading: showLoading,
    loadingTarget: loadingTarget
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 删除文件
 * @param data 请求数据
 */
export const del = (data: DeleteOrRestoreRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/delete',
    method: 'POST',
    data: data
  })
}

/**
 * 还原文件
 * @param data 请求数据
 */
export const restore = (data: DeleteOrRestoreRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/restore',
    method: 'POST',
    data: data
  })
}