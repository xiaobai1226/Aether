import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosResponse } from 'axios'
import { useAccountStore } from '@/stores/account'
import router from '@/router'

export interface NetdiskInternalAxiosRequestConfig extends InternalAxiosRequestConfig<any> {
  // 是否显示错误提示
  showErrMsg?: boolean;
  // 是否显示成功提示
  showSuccessMsg?: boolean;
}

interface NetdiskAxiosResponse extends AxiosResponse {
  config: NetdiskInternalAxiosRequestConfig;
}

// 创建 axios 实例
const httpInstance = axios.create({
  baseURL: import.meta.env.VITE_HTTP_BASE_URL,
  // 超时时间
  timeout: 30000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// 请求拦截器
httpInstance.interceptors.request.use((config: NetdiskInternalAxiosRequestConfig) => {
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
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
httpInstance.interceptors.response.use(
  (response: NetdiskAxiosResponse) => {
    const { code, msg } = response.data

    // 如果成功，返回data数据
    if (code === 200) {
      // 根据 showErrMsg 判断是否需要显示错误提示
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

    // ElMessage.error(msg || "系统出错");
    return Promise.reject(new Error(msg || 'Error'))
  },
  error => {
    if (error.response.data) {
      const { code, msg } = error.response.data

      // 如果是未登录错误
      if (code === 401) {
        const accountStore = useAccountStore()
        // 清除用户信息
        accountStore.clearAccountInfo()

        ElMessage.warning(msg)

        // 返回登录页
        router.push({ path: '/login' })
      }

      // 根据 showErrMsg 判断是否需要显示错误提示
      if (error.config.showErrMsg) {
        // 统一错误提示
        ElMessage.warning(msg)
      }
    }
    return Promise.reject(error)
  }
)

// 导出 axios 实例
export default httpInstance