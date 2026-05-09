package com.xiaobai1226.aether.common.enums;

import lombok.Getter;

/**
 * 存储源状态枚举
 *
 * @author bai
 */
@Getter
public enum StorageSourceStatusEnum {
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态描述
     */
    private final String desc;

    StorageSourceStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 根据状态值获取枚举
     *
     * @param status 状态值
     * @return 枚举
     */
    public static StorageSourceStatusEnum getByStatus(Integer status) {
        for (StorageSourceStatusEnum value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        return null;
    }
}

