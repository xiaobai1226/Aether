package com.xiaobai1226.aether.common.enums;

import lombok.Getter;

/**
 * 文件夹存储源类型枚举
 *
 * @author bai
 */
@Getter
public enum FolderStorageSourceTypeEnum {
    /**
     * 继承父目录
     */
    INHERIT(1, "继承父目录"),
    
    /**
     * 显式指定
     */
    EXPLICIT(2, "显式指定");

    /**
     * 类型值
     */
    private final Integer type;

    /**
     * 类型描述
     */
    private final String desc;

    FolderStorageSourceTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param type 类型值
     * @return 枚举
     */
    public static FolderStorageSourceTypeEnum getByType(Integer type) {
        for (FolderStorageSourceTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
    
    /**
     * 判断是否是继承类型
     *
     * @param type 类型值
     * @return 是否是继承类型
     */
    public static boolean isInherit(Integer type) {
        return INHERIT.type.equals(type);
    }
    
    /**
     * 判断是否是显式指定类型
     *
     * @param type 类型值
     * @return 是否是显式指定类型
     */
    public static boolean isExplicit(Integer type) {
        return EXPLICIT.type.equals(type);
    }
}