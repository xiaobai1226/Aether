package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * 预览本地文件DTO
 */
@Data
@AllArgsConstructor
public class PreviewLocalFileDTO {
    /**
     * 本地文件
     */
    private File file;

    /**
     * 响应文件名
     */
    private String fileName;
}