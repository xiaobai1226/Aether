import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useAccountStore } from '@/stores/account'
import router from '@/router'

// 创建 axios 实例
const httpInstance = axios.create({
  baseURL: import.meta.env.VITE_HTTP_BASE_URL,
  // 超时时间
  timeout: 30000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// 请求拦截器
httpInstance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
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
  (response: AxiosResponse) => {
    const { code, msg } = response.data
    // 如果成功，返回data数据
    if (code === 200) {
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
      // 如果是未登录错误
      if (error.response.status === 401) {
        // 错误提示
        ElMessage.warning('您还未登录，请登录后使用')

        const accountStore = useAccountStore()
        // 清除用户信息
        accountStore.clearAccountInfo()
        // 返回登录页
        router.push({ path: '/login' })
      } else {
        // 错误提示
        ElMessage.warning(error.message)
      }
    }
    return Promise.reject(error)
  }
)

// 导出 axios 实例
export default httpInstance