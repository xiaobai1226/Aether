package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 直链访问上下文DTO
 */
@Data
@AllArgsConstructor
public class DirectLinkAccessDTO {
    /**
     * 用户文件ID
     */
    private Long userFileId;

    /**
     * 所属用户ID
     */
    private Long userId;
}