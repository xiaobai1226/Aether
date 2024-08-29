package com.xiaobai1226.aether.core.enums;

/**
 * 验证码类型枚举类
 *
 * @author bai
 */
public enum CaptchaTypeEnum {

    /**
     * 图形验证码
     */
    IMAGE("image"),

    /**
     * 图形验证码
     */
    EMAIL("email");

    /**
     * 验证码type成员变量
     */
    private final String type;

    /**
     * 构造方法
     *
     * @param type 验证码type
     */
    CaptchaTypeEnum(String type) {
        this.type = type;
    }

    /**
     * 获取验证码type
     *
     * @return 验证码type
     */
    public String type() {
        return this.type;
    }
}
