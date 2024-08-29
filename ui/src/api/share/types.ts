/**
 * 获取用户文件请求信息
 */
export interface GetShareListByPageRequest {
    /**
     * 页码
     */
    pageNum: number;

    /**
     * 每页元素数
     */
    pageSize: number;
}

/**
 * 获取用户文件请求信息
 */
export interface GetShareFileListByShareIdPageRequest {
    /**
     * 分享ID
     */
    shareId: string;

    /**
     * 文件夹路径
     */
    path?: string;

    /**
     * 页码
     */
    pageNum: number;

    /**
     * 每页元素数
     */
    pageSize: number;
}

/**
 * 用户文件响应信息GetShareListByPaginationResponse TODO 暂时没有用到
 */
export interface GetShareListByPageResponse {
    /**
     * 页码
     */
    pageNum: number;

    /**
     * 每页条数
     */
    pageSize: number;

    /**
     * 总条数
     */
    total: number;

    /**
     * 总页数
     */
    totalPage: number;

    /**
     * 分页数据
     */
    list: Array<ShareFileInfo>;
}

/**
 * 分享文件信息
 */
export interface ShareFileInfo {

    /**
     * 分享ID
     */
    id: string;

    /**
     * 文件名称
     */
    name: string;

    /**
     * 文件类型 0 目录 1 文件
     */
    itemType: number;

    /**
     * 文件分类
     */
    category: number;

    /**
     * 缩略图
     */
    thumbnail: string;

    /**
     * 文件类型
     */
    fileType: number;

    /**
     * 提取码
     */
    extractionCode: string;

    /**
     * 浏览次数
     */
    viewNum: number;

    /**
     * 下载次数
     */
    downloadNum: number;

    /**
     * 保存次数
     */
    saveNum: number;

    /**
     * 有效期，单位 天，0为永久
     */
    validityPeriod: number;

    /**
     * 创建时间
     */
    createTime: string;
}

/**
 * 创建分享文件请求信息
 */
export interface CreateShareFileRequest {
    /**
     * 提取码，4位字符串，只包含字母数字
     */
    extractionCode?: string;

    /**
     * 分享内容ID集合，使用，分割的字符串
     */
    ids: string;

    /**
     * 有效期，单位 天，0为永久
     */
    validityPeriod: number;
}

/**
 * 创建分享文件响应信息
 */
export interface CreateShareFileResponse {
    /**
     * 分享ID
     */
    shareId: string;

    /**
     * 提取码，4位字符串，只包含字母数字
     */
    extractionCode: string;

    /**
     * 有效期，单位 天，0为永久
     */
    validityPeriod: number;
}

/**
 * 取消分享请求信息
 */
export interface CancelShareFileRequest {
    /**
     * 分享ID集合用逗号分割的字符串
     */
    ids: string;
}

/**
 * 获取分享信息响应
 */
export interface ShareInfoResponse {
    /**
     * 分享文件名称
     */
    name: string;

    /**
     * 分享时间
     */
    shareTime: string;

    /**
     * 用户昵称
     */
    nickname: string;

    /**
     * 用户头像
     */
    avatar: string;

    /**
     * 是否是当前用户
     */
    currentUser: boolean;
}

/**
 * 校验分享码请求信息
 */
export interface CheckExtractionCodeRequest {
    /**
     * 分享ID
     */
    shareId: string;

    /**
     * 提取码
     */
    extractionCode: string;
}