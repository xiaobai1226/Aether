package com.xiaobai1226.aether.core.enums;

/**
 * 用户文件状态枚举
 */
public enum UserFileStatusEnum {

    /**
     * 删除
     */
    DEL(0),

    /**
     * 正常
     */
    NORMAL(1);

    /**
     * flag成员变量
     */
    private final Integer flag;

    /**
     * 构造方法
     *
     * @param flag id
     */
    UserFileStatusEnum(Integer flag) {
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
}
