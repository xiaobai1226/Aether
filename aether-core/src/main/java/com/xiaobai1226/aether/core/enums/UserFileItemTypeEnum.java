package com.xiaobai1226.aether.core.enums;

/**
 * 用户文件类型枚举
 */
public enum UserFileItemTypeEnum {

    /**
     * 目录
     */
    FOLDER(0),

    /**
     * 文件
     */
    FILE(1);

    /**
     * flag成员变量
     */
    private final Integer flag;

    /**
     * 构造方法
     *
     * @param flag id
     */
    UserFileItemTypeEnum(Integer flag) {
        this.flag = flag;
    }

    /**
     * 获取flag
     *
     * @return flag
     */
    public Integer flag() {
        return this.flag;
    }

    /**
     * 判断是否是文件
     *
     * @return 是否是文件
     */
    public static boolean isFile(Integer flag) {
        return FILE.flag.equals(flag);

    }

    /**
     * 判断是否是文件
     *
     * @return 是否是文件
     */
    public static boolean isFile(UserFileItemTypeEnum userFileItemTypeEnum) {
        return FILE == userFileItemTypeEnum;
    }

    /**
     * 判断是否是文件夹
     *
     * @return 是否是文件夹
     */
    public static boolean isFolder(Integer flag) {
        return FOLDER.flag.equals(flag);

    }

    /**
     * 判断是否是文件夹
     *
     * @return 是否是文件夹
     */
    public static boolean isFolder(UserFileItemTypeEnum userFileItemTypeEnum) {
        return FOLDER == userFileItemTypeEnum;
    }

    /**
     * 根据flag获取枚举
     */
    public static UserFileItemTypeEnum getEnumByFlag(Integer flag) {
        for (var userFileItemTypeEnum : UserFileItemTypeEnum.values()) {
            if (userFileItemTypeEnum.flag.equals(flag)) {
                return userFileItemTypeEnum;
            }
        }
        return null;
    }
}
