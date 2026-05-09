package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缩略图生成结果DTO
 * 
 * @author bai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailGenerationResultDTO {

    /**
     * 是否生成成功
     */
    private boolean success;

    /**
     * 缩略图文件名（相对路径，如：2024/12/24/xxx.jpg）
     * 如果生成失败则为null
     */
    private String thumbnailFileName;

    /**
     * 缩略图完整路径（绝对路径）
     * 如果生成失败则为null
     */
    private String thumbnailFilePath;
}