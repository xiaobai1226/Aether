package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 创建下载结果DTO
 */
@Data
public class DownloadCreateResultDTO {
    /**
     * 下载类型：DIRECT 直接下载，TASK 任务下载
     */
    private String type;

    /**
     * 直接下载签名
     */
    private String sign;

    /**
     * 下载任务ID
     */
    private String taskId;
}