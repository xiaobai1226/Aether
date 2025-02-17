package com.xiaobai1226.aether.common.enums;

import lombok.AllArgsConstructor;

import java.util.Objects;

/**
 * 文件类型枚举
 */
@AllArgsConstructor
public enum FileTypeEnum {

    /**
     * Heic图片类型
     */
    HEIC("heic");

    /**
     * suffix
     */
    private final String suffix;

    /**
     * 是否是Heic格式
     */
    public static Boolean isHeic(String suffix) {
        return Objects.equals(HEIC.suffix, suffix);
    }
}