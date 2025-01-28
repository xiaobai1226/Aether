import httpInstance from '@/utils/http'
import type { LoginInfo, LoginUserInfo } from './types'
import type { AxiosPromise } from 'axios'
import { ApiVersion } from '@/api/ApiVersion'

const baseUrl = ApiVersion.API_V1

/**
 * 登录
 * @param data
 */
export const login = (data: LoginInfo): AxiosPromise<LoginUserInfo> => {
  return httpInstance({
    url: baseUrl + '/login',
    method: 'POST',
    data: data
  })
}

/**
 * 退出登录
 */
export const logout = (): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/logout',
    method: 'DELETE'
  })
}