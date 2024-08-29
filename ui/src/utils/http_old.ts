import axios from 'axios';
import type {AxiosRequestConfig, AxiosResponse} from 'axios';
import ElLoading from "element-plus";
import Message from "@/utils/Message";
import router from "@/router";

const contentTypeForm = 'application/x-www-form-urlencoded;charset=utf-8';
const contentTypeJson = "application/json;charset=utf-8";
const responseTypeJson = "json";
let loading = null;

// 创建 axios 实例
const httpInstance = axios.create({
    baseURL: "http://127.0.0.1:8080" + import.meta.env.VITE_APP_BASE_API,
    timeout: 30000,
    headers: {'Content-Type': 'application/json;charset=utf-8'}
});

// 请求拦截器
httpInstance.interceptors.request.use(
    (config) => {
        if (config.showLoading) {
            loading = ElLoading.service({
                lock: true,
                text: 'Loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
        }
        return config;
    },
    error => {
        if (config.showLoading && loading) {
            loading.close();
        }
        Message.error("请求发送失败");
        return Promise.reject(error);
    }
);

// 响应拦截器
httpInstance.interceptors.response.use(
    (response: AxiosResponse) => {
        const {showLoading, errorCallback, showError = true, responseType} = response.config as AxiosRequestConfig;
        if (showLoading && loading) {
            loading.close();
        }

        const responseData = response.data;
        if(responseType == "arraybuffer" || responseType == "blob") {
            return responseData;
        }
        if (responseData.code == 200) {
            return responseData;
        } else if (responseData.code == 901) {
            // 登录超时
            router.push({path: '/login?redirectUrl=' + encodeURI(router.currentRoute.value.path)});
            return Promise.reject({showError: false, msg: "登录超时"});
        } else {
            // 其他错误
            if (errorCallback) {
                errorCallback(responseData.info);
            }
            return Promise.reject({showError: showError, msg: responseData.info});
        }
    },
    error => {
        if (error.config.showLoading && loading) {
            loading.close();
        }
        return Promise.reject({showError: true, msg: "网络异常"});
    }
);

const request = (config) => {
    const {url, params, dataType, showLoading = true, responseType = responseTypeJson} = config;
    let contentType = contentTypeForm;
    // 创建form对象
    let formData = new FormData();
    for (let key in params) {
        formData.append(key, params[key] == undefined ? '' : params[key]);
    }
    if (dataType != null && dataType == 'json') {
        contentType = contentTypeJson;
    }
    let headers = {
        'Content-Type': contentType,
        "X-Requested-With": "XMLHttpRequest"
    };

    return httpInstance.post(url, formData, {
        onUploadProgress: (progressEvent) => {
            if(config.uploadProgressCallback) {
                config.uploadProgressCallback(progressEvent);
            }
        },
        responseType: responseType,
        headers: headers,
        showLoading: showLoading,
        errorCallback: config.errorCallback,
        showError: config.showError,
    }).catch(error => {
        console.log(error);
        if (error.showError) {
            Message.error(error.msg);
        }
        return null;
    });
}

// 导出 axios 实例
export default httpInstance;