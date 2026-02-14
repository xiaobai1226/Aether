package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 下载任务状态DTO
 */
@Data
public class DownloadTaskStatusDTO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 下载文件名
     */
    private String fileName;

    /**
     * 任务下载签名
     */
    private String downloadSign;

    /**
     * 需要打包的文件总数
     */
    private Integer totalFileCount;

    /**
     * 已打包的文件数量
     */
    private Integer completedFileCount;

    /**
     * 失败原因
     */
    private String errorMsg;
}
