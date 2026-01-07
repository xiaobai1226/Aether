package com.xiaobai1226.aether.core.service.intf;

/**
 * 配额/空间服务（集中管理用户空间校验、上传预占、已用空间增减）
 *
 * 设计目标：把空间相关规则收敛为唯一入口，避免在上传/复制/彻底删除等多流程里重复写 usedStorage 与 uploading 逻辑。
 */
public interface QuotaService {

    /**
     * 校验是否有足够空间（考虑 uploading 预占）
     *
     * @param userId 用户ID
     * @param bytes  需要占用的空间（字节）
     */
    void checkEnough(Long userId, long bytes);

    /**
     * 预占上传空间（会先校验空间是否足够）
     *
     * @param userId 用户ID
     * @param bytes  预占空间（字节）
     */
    void reserveUploading(Long userId, long bytes);

    /**
     * 释放上传预占空间
     *
     * @param userId 用户ID
     * @param bytes  释放空间（字节）
     */
    void releaseUploading(Long userId, long bytes);

    /**
     * 增加用户已用空间（usedStorage）
     *
     * @param userId 用户ID
     * @param bytes  增加空间（字节）
     */
    void increaseUsed(Long userId, long bytes);

    /**
     * 减少用户已用空间（usedStorage）
     *
     * @param userId 用户ID
     * @param bytes  减少空间（字节）
     */
    void decreaseUsed(Long userId, long bytes);
}