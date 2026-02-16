package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 文件直链记录DTO
 */
@Data
public class DirectLinkRecordDTO {
    /**
     * 直链token
     */
    private String token;

    /**
     * 用户文件ID
     */
    private Long userFileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 所在目录
     */
    private String folderPath;

    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 状态 1 启用 0 失效
     */
    private Integer status;

    /**
     * 过期时间，NULL为永久
     */
    private String expireAt;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 业务状态：ACTIVE 启用，EXPIRED 已过期，DISABLED 已失效
     */
    private String bizStatus;
}
