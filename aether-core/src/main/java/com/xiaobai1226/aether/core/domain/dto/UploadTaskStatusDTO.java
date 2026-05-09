package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 上传任务状态 DTO
 */
@Data
public class UploadTaskStatusDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 上传状态
     */
    private Integer status;

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