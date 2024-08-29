// 管理用户相关数据
import {defineStore} from 'pinia';
import {ref} from 'vue';
import {getUserSpaceUsage} from "@/api/user";
import type {UserSpaceUsage} from "@/api/user/types";

export const useUserStore = defineStore('user', () => {
    // 使用空间
    const useSpaceInfo = ref<UserSpaceUsage>({
        usedStorage: 0,
        totalStorage: 0,
        remainStorage: 0,
        uploadingUsedStorage: 0,
        realRemainStorage: 0
    });

    // 请求用户空间使用情况
    const handleGetUserSpaceUsage = () => {
        getUserSpaceUsage().then(({data}) => {
            useSpaceInfo.value = data;
        });
    }

    // 以对象的格式把state和action返回
    return {useSpaceInfo, handleGetUserSpaceUsage}
});