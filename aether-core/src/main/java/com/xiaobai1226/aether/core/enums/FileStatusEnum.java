package com.xiaobai1226.aether.core.enums;

/**
 * 文件状态枚举
 *
 * @author bai
 */
public enum FileStatusEnum {

    /**
     * 转码中
     */
    TRANSFER(0),

    /**
     * 转码成功
     */
    NORMAL(1),

    /**
     * 转码失败
     */
    TRANSFER_FAIL(2);

    /**
     * flag成员变量
     */
    private final Integer flag;

    /**
     * 构造方法
     *
     * @param flag id
     */
    FileStatusEnum(Integer flag) {
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
