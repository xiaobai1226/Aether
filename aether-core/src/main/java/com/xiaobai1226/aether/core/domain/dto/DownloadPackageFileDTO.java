package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * 打包下载文件DTO
 */
@Data
@AllArgsConstructor
public class DownloadPackageFileDTO {
    /**
     * 打包后的ZIP文件
     */
    private File file;

    /**
     * 对外下载文件名
     */
    private String fileName;
}