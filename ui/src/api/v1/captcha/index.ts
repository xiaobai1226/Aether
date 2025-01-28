import httpInstance from '@/utils/http'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'
import type { ImageCaptchaInfo } from './types'
import type { AxiosPromise } from 'axios'
import { ApiVersion } from '@/api/ApiVersion'

const baseUrl = ApiVersion.API_V1 + '/captcha'

/**
 * 获取图形验证码
 */
export const getImageCaptcha = (): AxiosPromise<ImageCaptchaInfo> => {

  const url = baseUrl + '/getImageCaptcha'
  return httpInstance.get(url, { showSuccessMsg: false, showLoading: false } as NetdiskInternalAxiosRequestConfig)
}