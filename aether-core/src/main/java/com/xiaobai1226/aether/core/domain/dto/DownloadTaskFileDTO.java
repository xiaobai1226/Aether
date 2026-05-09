package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * 下载任务文件DTO
 */
@Data
@AllArgsConstructor
public class DownloadTaskFileDTO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务文件
     */
    private File file;

    /**
     * 下载文件名
     */
    private String fileName;
}
