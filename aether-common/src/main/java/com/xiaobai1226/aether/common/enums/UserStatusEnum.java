package com.xiaobai1226.aether.common.enums;

/**
 * 用户状态枚举类
 *
 * @author bai
 */
public enum UserStatusEnum {

    /**
     * 正常
     */
    NORMAL(1),

    /**
     * 禁用
     */
    BAN(0);

    /**
     * flag成员变量
     */
    private final Integer flag;

    /**
     * 构造方法
     *
     * @param flag id
     */
    UserStatusEnum(Integer flag) {
        this.flag = flag;
    }

    /**
     * 获取事件id
     *
     * @return 事件id
     */
    public Integer flag() {
        return this.flag;
    }
}
