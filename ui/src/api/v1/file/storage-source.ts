/**
 * 文件相关的存储源API
 */

import http from '@/utils/http'
import { ApiVersion } from '@/api/ApiVersion'
import type { SetFolderStorageSourceRequest } from '../storage-source/types'

const baseUrl = ApiVersion.API_V1 + '/file'

/**
 * 设置文件夹存储源
 */
export function setFolderStorageSource(data: SetFolderStorageSourceRequest) {
  return http({
    url: baseUrl + '/setFolderStorageSource',
    method: 'POST',
    data
  })
}

