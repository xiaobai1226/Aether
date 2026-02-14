package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 下载任务缓存实体
 */
@Data
public class DownloadTaskDTO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 目标文件ID
     */
    private List<Long> ids;

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
     * 临时文件绝对路径
     */
    private String filePath;

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
     * 创建时间（毫秒时间戳）
     */
    private Long createTime;

    /**
     * 是否已触发下载
     */
    private Boolean downloaded;

    /**
     * 下载完成时间（毫秒时间戳）
     */
    private Long downloadTime;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 过期时间（毫秒时间戳）
     */
    private Long expireTime;
}
