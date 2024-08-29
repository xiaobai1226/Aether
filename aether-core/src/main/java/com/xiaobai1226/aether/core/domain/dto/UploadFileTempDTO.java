package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 上传文件临时DTO
 *
 * @author bai
 */
@Data
public class UploadFileTempDTO {

    /**
     * 父文件夹ID
     */
    private Integer parentId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * md5码
     */
    private String identifier;

    /**
     * 已上传大小
     */
    private Long uploadedSize;

    /**
     * 总切片数
     */
    private Integer totalChunks;

    /**
     * 获取临时文件目录
     */
    private String tempFolder;
}
