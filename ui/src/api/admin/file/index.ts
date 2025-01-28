import { ApiVersion } from '@/api/ApiVersion'
import httpInstance from '@/utils/http'
import type { AxiosPromise } from 'axios'
import type {
  GetFileListByPageRequest,
  GetFileListByPageResponse
} from '@/api/admin/file/types'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'

const baseUrl = ApiVersion.API_ADMIN + '/file'

/**
 * 分页获取用户列表
 * @param params
 */
export const getFileListByPage = (params: GetFileListByPageRequest): AxiosPromise<GetFileListByPageResponse> => {
  const url = baseUrl + '/getFileListByPage'
  return httpInstance.get(url, {
    params: params,
    showSuccessMsg: false,
    showLoading: true
  } as NetdiskInternalAxiosRequestConfig)
}