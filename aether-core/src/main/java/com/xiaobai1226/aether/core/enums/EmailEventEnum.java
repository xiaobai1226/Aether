package com.xiaobai1226.aether.core.enums;

/**
 * 邮件事件枚举类
 *
 * @author bai
 */
public enum EmailEventEnum {

    /**
     * 注册
     */
    EVENT_REGISTER(0, "register"),

    /**
     * 重置密码
     */
    EVENT_RESET_PASSWORD(1, "resetPassword");

    /**
     * 事件value成员变量
     */
    private final Integer eventId;

    /**
     * 事件name成员变量
     */
    private final String eventName;

    /**
     * 构造方法
     *
     * @param eventId   事件id
     * @param eventName 事件name
     */
    EmailEventEnum(Integer eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }

    /**
     * 获取事件id
     *
     * @return 事件id
     */
    public Integer eventId() {
        return this.eventId;
    }

    /**
     * 获取事件name
     *
     * @return 事件name
     */
    public String eventName() {
        return this.eventName;
    }
}
