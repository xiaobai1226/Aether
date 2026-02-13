package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 单切片上传结果 DTO
 */
@Data
public class UploadChunkResultDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 上传状态
     */
    private Integer status;

    /**
     * 当前切片索引
     */
    private Integer chunkIndex;

    /**
     * 已上传字节数
     */
    private Long uploadedSize;

    /**
     * 已接收切片数量
     */
    private Integer receivedChunks;
}