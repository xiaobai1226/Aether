import httpInstance from "@/utils/http";
import type {LoginInfo, LoginUserInfo} from "./types";
import type {AxiosPromise} from "axios";

/**
 * 登录
 * @param data
 */
export const login = (data: LoginInfo): AxiosPromise<LoginUserInfo> => {
    return httpInstance({
        url: '/login',
        method: 'POST',
        data: data
    });
}

/**
 * 退出登录
 */
export const logout = (): AxiosPromise => {
    return httpInstance({
        url: '/logout',
        method: 'DELETE'
    });
}