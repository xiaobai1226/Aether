// 管理用户相关数据
import {defineStore} from 'pinia';
import {ref} from 'vue';

export const useUploaderStore = defineStore('uploader', () => {
    /**
     * 是否展示上传页面状态，默认为false
     */
    const isShowUploader = ref(false);

    /**
     * 修改是否展示上传页面状态
     */
    const updateShowUploader = (value: boolean) => {
        isShowUploader.value = value;
    }

    /**
     * Uploader的增加上传文件的方法
     */
    let addUploadFileOfUploader = null as unknown as ((file: File, uid: string, path: string | null, uploadedCallback: () => void) => void) | null;

    /**
     * 初始化Uploader的增加上传文件的方法
     * @param method
     */
    const initAddUploadFileOfUploader = (method: (file: File, uid: string, path: string | null, uploadedCallback: () => void) => void) => {
        addUploadFileOfUploader = method;
    }

    /**
     * 增加上传文件的方法
     */
    const addUploadFile = (file: File, uid: string, path: string | null, uploadedCallback: () => void) => {
        if (addUploadFileOfUploader != null) {
            isShowUploader.value = true;
            // 调用Uploader的增加上传文件的方法
            addUploadFileOfUploader(file, uid, path, uploadedCallback);
        }
    }

    // 以对象的格式把state和action返回
    return {isShowUploader, updateShowUploader, initAddUploadFileOfUploader, addUploadFile};
});