package com.xiaobai1226.aether.core.enums;

/**
 * 系统枚举
 */
public enum OsEnum {
    /**
     * Windows
     */
    WIN("win"),

    /**
     * Mac
     */
    MAC("mac"),

    /**
     * Linux
     */
    LINUX("linux"),

    /**
     * unknow
     */
    UNKNOW("unknow");

    /**
     * 系统标识
     */
    private final String flag;

    /**
     * 构造方法
     *
     * @param flag 系统标识
     */
    OsEnum(String flag) {
        this.flag = flag;
    }

    /**
     * 获取系统标识
     *
     * @return 系统标识
     */
    public String flag() {
        return this.flag;
    }

    /**
     * 获取当前系统
     */
    public static OsEnum getCurrentOs() {
        String osName = System.getProperty("os.name").toLowerCase();
        String binaryFilePath;
        if (osName.contains("win")) {
            return OsEnum.WIN;
        } else if (osName.contains("mac")) {
            return OsEnum.MAC;
        } else if (osName.contains("nix") || osName.contains("nux")) {
            return OsEnum.LINUX;
        }

        return OsEnum.UNKNOW;
    }
}
