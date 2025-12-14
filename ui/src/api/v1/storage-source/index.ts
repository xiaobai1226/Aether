/**
 * 存储源相关API
 */

import http from '@/utils/http'
import { ApiVersion } from '@/api/ApiVersion'
import type {
  StorageSourceDTO,
  AddStorageSourceRequest,
  UpdateStorageSourceRequest
} from './types'

const baseUrl = ApiVersion.API_V1 + '/storageSource'

/**
 * 获取用户的存储源列表
 */
export function getStorageSourceList() {
  return http<StorageSourceDTO[]>({
    url: baseUrl + '/list',
    method: 'GET'
  })
}

/**
 * 检查用户是否有存储源
 */
export function hasStorageSource() {
  return http<boolean>({
    url: baseUrl + '/hasStorageSource',
    method: 'GET'
  })
}

/**
 * 添加存储源
 */
export function addStorageSource(data: AddStorageSourceRequest) {
  return http({
    url: baseUrl + '/add',
    method: 'POST',
    data
  })
}

/**
 * 更新存储源
 */
export function updateStorageSource(data: UpdateStorageSourceRequest) {
  return http({
    url: baseUrl + '/update',
    method: 'POST',
    data
  })
}

/**
 * 删除存储源
 */
export function deleteStorageSource(id: number) {
  return http({
    url: baseUrl + '/delete',
    method: 'POST',
    params: { id }
  })
}

/**
 * 设置默认存储源
 */
export function setDefaultStorageSource(id: number) {
  return http({
    url: baseUrl + '/setDefault',
    method: 'POST',
    params: { id }
  })
}

