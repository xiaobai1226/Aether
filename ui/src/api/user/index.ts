import httpArrayBufferInstance from '@/utils/HttpArrayBuffer'
import httpInstance from '@/utils/http'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'
import type { AxiosPromise } from 'axios'
import type { UserSpaceUsage } from '@/api/user/types'

const baseUrl = '/user'

/**
 * 获取头像
 */
export const getAvatar = (): AxiosPromise<ArrayBuffer> => {
  return httpArrayBufferInstance({
    url: baseUrl + '/getAvatar',
    method: 'GET',
    responseType: 'arraybuffer'
  })
}

/**
 * 更新用户头像
 * @param file 头像文件
 */
export const updateUserAvatar = (file: File): AxiosPromise => {
  const formData = new FormData()
  formData.append('avatar', file)

  return httpInstance({
    url: baseUrl + '/updateUserAvatar',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新密码
 * @param password 密码
 */
export const updateUserPassword = (password: string): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/updateUserPassword',
    method: 'POST',
    data: { password: password }
  })
}

/**
 * 获取用户存储空间
 */
export const getUserSpaceUsage = (): AxiosPromise<UserSpaceUsage> => {
  const url = baseUrl + '/getUserSpaceUsage'
  return httpInstance.get(url, { showSuccessMsg: false, showLoading: false } as NetdiskInternalAxiosRequestConfig)
}