package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 创建文件直链结果DTO
 */
@Data
public class DirectLinkCreateResultDTO {
    /**
     * 直链token
     */
    private String token;

    /**
     * 过期时间，NULL为永久
     */
    private String expireAt;
}