package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 存储源数据传输对象DTO
 *
 * @author bai
 */
@Data
public class StorageSourceDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 存储源名称
     */
    private String name;

    /**
     * 存储源类型 0=本地存储
     */
    private Integer type;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 是否为默认存储源 0=否 1=是
     */
    private Integer isDefault;

    /**
     * 状态 0=禁用 1=启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}

