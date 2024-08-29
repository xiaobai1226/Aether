/**
 * 用户空间使用情况
 */
export interface UserSpaceUsage {
    /**
     * 已使用存储空间 单位 byte
     */
    usedStorage: number;

    /**
     * 总存储空间 单位 byte
     */
    totalStorage: number;

    /**
     * 剩余存储空间 单位 byte
     */
    remainStorage: number;

    /**
     * 上传中已使用存储空间 单位 byte
     */
    uploadingUsedStorage: number;

    /**
     * 实际剩余存储空间 单位 byte
     */
    realRemainStorage: number;
}