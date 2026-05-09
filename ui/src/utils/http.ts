import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import type { AxiosResponse } from 'axios'
import { useAccountStore } from '@/stores/account'
import router from '@/router'

export interface NetdiskInternalAxiosRequestConfig extends InternalAxiosRequestConfig<any> {
  // 是否显示错误提示
  showErrMsg?: boolean;
  // 是否显示成功提示
  showSuccessMsg?: boolean;
  // 是否显示Loading加载
  showLoading?: boolean;
  // loading的对象
  loadingTarget?: HTMLElement;
}

interface NetdiskAxiosResponse extends AxiosResponse {
  config: NetdiskInternalAxiosRequestConfig;
}

let loading: any = null

const requestInterceptor = (config: NetdiskInternalAxiosRequestConfig) => {

  // 如果 showLoading 属性没有被提供，那么将其设为 true
  if (config.showLoading === undefined) {
    config.showLoading = true
  }

  if (config.showLoading) {
    if (config.loadingTarget) {
      // 开启Loading
      loading = ElLoading.service({ target: config.loadingTarget })
    } else {
      // 开启Loading
      loading = ElLoading.service({ fullscreen: true })
    }
  }

  // 如果 showErrMsg 属性没有被提供，那么将其设为 true
  if (config.showErrMsg === undefined) {
    config.showErrMsg = true
  }

  // 如果 showSuccessMsg 属性没有被提供，那么将其设为 true
  if (config.showSuccessMsg === undefined) {
    config.showSuccessMsg = true
  }

  // 从pinia获取token数据
  const accountStore = useAccountStore()
  const { tokenName, tokenPrefix, token } = accountStore.accountInfo
  // 按照后端要求拼接token数据
  if (tokenName && tokenPrefix && token) {
    config.headers[tokenName] = tokenPrefix + ' ' + token
  }

  return config
}

const responseSuccessInterceptor = (response: NetdiskAxiosResponse) => {
  const { code, msg } = response.data

  // 根据 showLoading 判断是否要观察Loading
  if (response.config.showLoading && loading) {
    // 关闭Loading
    loading.close()
  }

  // 如果成功，返回data数据
  if (code === 200) {
    // 根据 showSuccessMsg 判断是否需要显示错误提示
    if (response.config.showSuccessMsg) {
      // 统一错误提示
      ElMessage.success(msg)
    }

    return response.data
  }
  // 如果响应数据为二进制流，返回二进制流
  if (response.data instanceof ArrayBuffer) {
    return response
  }

  return Promise.reject(new Error(msg || 'Error'))
}

const responseErrorInterceptor = (error: any) => {
  // 根据 showLoading 判断是否要观察Loading
  if (error?.config?.showLoading && loading) {
    // 关闭Loading
    loading.close()
  }

  if (error?.response?.data) {
    const { code, msg } = error.response.data

    // 如果是未登录错误
    if (code === 401) {
      const accountStore = useAccountStore()
      // 清除用户信息
      accountStore.clearAccountInfo()

      ElMessage.warning(msg)

      // 保存当前路由信息并跳转到登录页
      const currentRoute = router.currentRoute.value
      if (currentRoute.path !== '/login') {
        router.push({
          path: '/login',
          query: {
            redirect: currentRoute.fullPath
          }
        })
      }
    }

    // 根据 showErrMsg 判断是否需要显示错误提示
    if (error?.config?.showErrMsg) {
      // 统一错误提示
      ElMessage.warning(msg)
    }
  }
  return Promise.reject(error)
}

const setupInterceptors = (instance: any) => {
  instance.interceptors.request.use(requestInterceptor, (error: any) => Promise.reject(error))
  instance.interceptors.response.use(responseSuccessInterceptor, responseErrorInterceptor)
}

// 创建 axios 实例
const httpInstance = axios.create({
  baseURL: import.meta.env.VITE_HTTP_BASE_URL,
  // 超时时间
  timeout: 30000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// 上传专用 axios 实例（弱网下使用更长超时）
export const uploadHttpInstance = axios.create({
  baseURL: import.meta.env.VITE_HTTP_BASE_URL,
  timeout: 180000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

setupInterceptors(httpInstance)
setupInterceptors(uploadHttpInstance)

// 导出 axios 实例
export default httpInstance