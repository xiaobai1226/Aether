package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 上传结果DTO实体类
 */
@Data
@AllArgsConstructor
public class UploadResultDTO {

    /**
     * 任务ID
     */
    public String taskId;

    /**
     * 上传状态
     */
    public Integer status;
}
