package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * 本地直连下载文件DTO
 */
@Data
@AllArgsConstructor
public class DownloadLocalFileDTO {
    /**
     * 本地文件
     */
    private File file;

    /**
     * 下载文件名
     */
    private String fileName;
}
