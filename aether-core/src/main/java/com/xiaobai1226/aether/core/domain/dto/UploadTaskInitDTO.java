package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 上传任务初始化结果 DTO
 */
@Data
public class UploadTaskInitDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 上传状态
     */
    private Integer status;

    /**
     * 建议切片大小（字节）
     */
    private Long chunkSize;

    /**
     * 总切片数
     */
    private Integer totalChunks;

    /**
     * 已上传切片
     */
    private List<Integer> uploadedChunks;

    /**
     * 已上传字节数
     */
    private Long uploadedSize;
}