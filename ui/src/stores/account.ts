// 管理用户相关数据
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login } from '@/api/v1/account'
import type { LoginInfo, LoginUserInfo } from '@/api/v1/account/types'

export const useAccountStore = defineStore('account', () => {
  // 定义管理用户数据的state
  const accountInfo = ref<LoginUserInfo>({
    nickname: '',
    // avatar: '',
    usedStorage: 0,
    totalStorage: 0,
    roleId: 0,
    token: '',
    tokenName: '',
    tokenPrefix: ''
  })

  // 登录
  const accountLogin = (loginInfo: LoginInfo) => {
    return new Promise<void>((resolve, reject) => {
      login(loginInfo)
        .then((response) => {
          // 如果服务器返回的数据中没有昵称，使用用户名作为昵称
          const userData = response.data;
          if (!userData.nickname) {
            userData.nickname = loginInfo.username;
          }
          accountInfo.value = userData;
          resolve()
        })
        .catch((error) => {
          reject(error)
        })
    })
  }

  // 清除用户信息
  const clearAccountInfo = () => {
    accountInfo.value = {
      nickname: '',
      // avatar: '',
      usedStorage: 0,
      totalStorage: 0,
      roleId: 0,
      token: '',
      tokenName: '',
      tokenPrefix: ''
    }
  }

  // 以对象的格式把state和action返回
  return { accountInfo, accountLogin, clearAccountInfo }
}, {
  persist: true
})