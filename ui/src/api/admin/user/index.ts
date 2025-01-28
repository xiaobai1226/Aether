import { ApiVersion } from '@/api/ApiVersion'
import httpInstance from '@/utils/http'
import type { AxiosPromise } from 'axios'
import type {
  GetUserListByPageRequest,
  GetUserListByPageResponse,
  AddUserRequest,
  UpdateUserRequest
} from '@/api/admin/user/types'
import type { NetdiskInternalAxiosRequestConfig } from '@/utils/http'

const baseUrl = ApiVersion.API_ADMIN + '/user'

/**
 * 分页获取用户列表
 * @param params
 */
export const getUserListByPage = (params: GetUserListByPageRequest): AxiosPromise<GetUserListByPageResponse> => {
  const url = baseUrl + '/getUserListByPage'
  return httpInstance.get(url, {
    params: params,
    showSuccessMsg: false,
    showLoading: true
  } as NetdiskInternalAxiosRequestConfig)
}

/**
 * 新增用户
 * @param addUserRequest 新增用户信息
 */
export const addUser = (addUserRequest: AddUserRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/addUser',
    method: 'POST',
    data: addUserRequest
  })
}

/**
 * 修改用户
 * @param updateUserRequest 修改用户信息
 */
export const updateUser = (updateUserRequest: UpdateUserRequest): AxiosPromise => {
  return httpInstance({
    url: baseUrl + '/updateUser',
    method: 'POST',
    data: updateUserRequest
  })
}