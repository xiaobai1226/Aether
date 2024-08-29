import httpInstance from "@/utils/http";
import type {NetdiskInternalAxiosRequestConfig} from "@/utils/http";
import type {AxiosPromise} from "axios";
import type {
    DeleteOrRestoreRequest,
    GetRecycleBinListByPageRequest,
    GetRecycleBinListByPageResponse
} from "@/api/recycleBin/types";

const baseUrl = '/recyclebin';

/**
 * 分页获取文件列表
 * @param params
 */
export const getRecycleBinListByPage = (params: GetRecycleBinListByPageRequest): AxiosPromise<GetRecycleBinListByPageResponse> => {
    const url = baseUrl + '/getRecycleBinListByPage';
    return httpInstance.get(url, {
        params: params,
        showSuccessMsg: false,
    } as NetdiskInternalAxiosRequestConfig);
}

/**
 * 删除文件
 * @param data 请求数据
 */
export const del = (data: DeleteOrRestoreRequest): AxiosPromise => {
    return httpInstance({
        url: baseUrl + '/delete',
        method: 'POST',
        data: data
    });
}

/**
 * 还原文件
 * @param data 请求数据
 */
export const restore = (data: DeleteOrRestoreRequest): AxiosPromise => {
    return httpInstance({
        url: baseUrl + '/restore',
        method: 'POST',
        data: data
    });
}