package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 下载文件实体类DTO
 *
 * @author bai
 */
@Data
@AllArgsConstructor
public class DownloadFileDTO {

    /**
     * 文件ID
     */
    private Integer id;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件路径
     */
    private String path;

}