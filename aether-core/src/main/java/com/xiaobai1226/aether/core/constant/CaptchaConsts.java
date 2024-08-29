package com.xiaobai1226.aether.core.constant;

/**
 * 验证码常量类
 *
 * @author bai
 */
public class CaptchaConsts {

    ///////////////////////////////////////////////// 图形验证码 ///////////////////////////////////////////////

    /**
     * 图形验证码的字符数
     */
    public static final int IMAGE_CAPTCHA_LENGTH = 4;

    /**
     * 图形验证码的长
     */
    public static final int IMAGE_CAPTCHA_WIDTH = 150;

    /**
     * 图形验证码的宽
     */
    public static final int IMAGE_CAPTCHA_HEIGHT = 45;

    /**
     * 图形验证码的干扰元素个数
     */
    public static final int IMAGE_CAPTCHA_CIRCLE_COUNT = 4;

    /**
     * 图形验证码的过期时间 单位：分钟
     */
    public static final int IMAGE_CAPTCHA_TIMEOUT = 5;


    ///////////////////////////////////////////////// 邮箱验证码 ///////////////////////////////////////////////

    /**
     * 邮箱验证码的字符数
     */
    public static final int EMAIL_CAPTCHA_LENGTH = 6;

    /**
     * 邮箱验证码的过期时间 单位：分钟
     */
    public static final int EMAIL_CAPTCHA_TIMEOUT = 15;

    /**
     * 邮箱验证码标题
     */
    public static final String EMAIL_CAPTCHA_SUBJECT = "邮箱验证码";

    /**
     * 邮箱验证码内容
     */
    public static final String EMAIL_CAPTCHA_CONTENT = "您好，您的邮箱验证码为：%s，有效期为" + EMAIL_CAPTCHA_TIMEOUT + "分钟，请勿泄露给他人。";
}
