package com.xiaobai1226.aether.common.enums;

import lombok.Getter;

/**
 * 存储源类型枚举
 *
 * @author bai
 */
@Getter
public enum StorageSourceTypeEnum {
    /**
     * 本地存储
     */
    LOCAL(0, "本地存储");

    /**
     * 类型值
     */
    private final Integer type;

    /**
     * 类型描述
     */
    private final String desc;

    StorageSourceTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param type 类型值
     * @return 枚举
     */
    public static StorageSourceTypeEnum getByType(Integer type) {
        for (StorageSourceTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}