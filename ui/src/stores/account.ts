// 管理用户相关数据
import {defineStore} from 'pinia';
import {ref} from 'vue';
import {login} from "@/api/account";
import type {LoginInfo, LoginUserInfo} from "@/api/account/types";

export const useAccountStore = defineStore('account', () => {
    // 定义管理用户数据的state
    const accountInfo = ref<LoginUserInfo>({
        nickname: '',
        // avatar: '',
        usedStorage: 0,
        totalStorage: 0,
        // roleId: 0,
        token: '',
        tokenName: '',
        tokenPrefix: '',
    });

    // 登录
    const accountLogin = (loginInfo: LoginInfo) => {
        return new Promise<void>((resolve, reject) => {
            login(loginInfo)
                .then((response) => {
                    accountInfo.value = response.data;
                    resolve();
                })
                .catch((error) => {
                    reject(error);
                });
        });
    }

    // 清除用户信息
    const clearAccountInfo = () => {
        accountInfo.value = {
            nickname: '',
            // avatar: '',
            usedStorage: 0,
            totalStorage: 0,
            // roleId: 0,
            token: '',
            tokenName: '',
            tokenPrefix: '',
        };
    }

    // 以对象的格式把state和action返回
    return {accountInfo, accountLogin, clearAccountInfo}
}, {
    persist: true,
});