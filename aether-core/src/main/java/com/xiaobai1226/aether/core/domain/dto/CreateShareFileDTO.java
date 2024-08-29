package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 创建分享文件DTO
 *
 * @author bai
 */
@Data
@AllArgsConstructor
public class CreateShareFileDTO {

    /**
     * 分享ID
     */
    private String shareId;

    /**
     * 提取码，4位字符串，只包含字母数字
     */
    private String extractionCode;

    /**
     * 有效期，单位 天，0为永久
     */
    private Integer validityPeriod;
}
