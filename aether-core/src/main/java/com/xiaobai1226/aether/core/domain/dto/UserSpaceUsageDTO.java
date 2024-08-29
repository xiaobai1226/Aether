package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户空间使用情况DTO
 */
@Data
@AllArgsConstructor
public class UserSpaceUsageDTO {

    /**
     * 已使用存储空间 单位 byte
     */
    private Long usedStorage;

    /**
     * 总存储空间 单位 byte
     */
    private Long totalStorage;

    /**
     * 剩余存储空间 单位 byte
     */
    private Long remainStorage;

    /**
     * 上传中已使用存储空间 单位 byte
     */
    private Long uploadingUsedStorage;

    /**
     * 实际剩余存储空间 单位 byte
     */
    private Long realRemainStorage;
}
