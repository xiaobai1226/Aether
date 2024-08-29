import httpInstance from "@/utils/http";
import type {NetdiskInternalAxiosRequestConfig} from "@/utils/http";
import type {AxiosPromise} from "axios";
import type {
    CancelShareFileRequest, CheckExtractionCodeRequest,
    CreateShareFileRequest, CreateShareFileResponse, GetShareFileListByShareIdPageRequest,
    GetShareListByPageRequest, GetShareListByPageResponse, ShareInfoResponse
} from "@/api/share/types";
import type {GetFileListByPageResponse} from "@/api/file/types";

const baseUrl = '/share';

/**
 * 分页获取文件列表
 * @param params
 */
export const getShareListByPage = (params: GetShareListByPageRequest): AxiosPromise<GetShareListByPageResponse> => {
    const url = baseUrl + '/getShareListByPage';
    return httpInstance.get(url, {
        params: params,
        showSuccessMsg: false,
    } as NetdiskInternalAxiosRequestConfig);
}

/**
 * 分页获取分享文件详情列表
 * @param params
 */
export const getShareFileListByShareIdPagination = (params: GetShareFileListByShareIdPageRequest): AxiosPromise<GetFileListByPageResponse> => {
    const url = baseUrl + '/getShareFileListByShareIdPage';
    return httpInstance.get(url, {
        params: params,
        showSuccessMsg: false,
    } as NetdiskInternalAxiosRequestConfig);
}

/**
 * 创建分享文件
 * @param data 请求数据
 */
export const create = (data: CreateShareFileRequest): AxiosPromise<CreateShareFileResponse> => {
    return httpInstance({
        url: baseUrl + '/create',
        method: 'POST',
        data: data
    });
}

/**
 * 取消分享文件
 * @param data 请求数据
 */
export const cancel = (data: CancelShareFileRequest): AxiosPromise => {
    return httpInstance({
        url: baseUrl + '/cancel',
        method: 'POST',
        data: data
    });
}

/**
 * 获取分享信息
 * @param shareId
 */
export const getShareInfo = (shareId: string): AxiosPromise<ShareInfoResponse> => {
    const url = baseUrl + '/getShareInfo';
    return httpInstance.get(url, {
        params: {shareId: shareId},
        showSuccessMsg: false,
    } as NetdiskInternalAxiosRequestConfig);
}

/**
 * 获取分享信息
 * @param shareId
 */
export const getShareLoginInfo = (shareId: string): AxiosPromise<ShareInfoResponse> => {
    const url = baseUrl + '/getShareLoginInfo';
    return httpInstance.get(url, {
        params: {shareId: shareId},
        showSuccessMsg: false,
    } as NetdiskInternalAxiosRequestConfig);
}


/**
 * 校验分享码
 * @param data 请求数据
 */
export const checkExtractionCode = (data: CheckExtractionCodeRequest): AxiosPromise => {
    return httpInstance({
        url: baseUrl + '/checkExtractionCode',
        method: 'POST',
        data: data
    });
}