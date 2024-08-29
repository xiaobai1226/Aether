package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

import java.io.File;

/**
 * 上传文件缓存DTO
 */
@Data
public class UploadFileCacheDTO {

    /**
     * 临时文件
     */
    private File tempDir;

    /**
     * 最终文件存储位置
     */
    private String finalFilePath;

    /**
     * 缩略图存储位置
     */
    private String thumbnailFilePath;
}