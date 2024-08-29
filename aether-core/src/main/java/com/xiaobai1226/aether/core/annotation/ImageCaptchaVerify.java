package com.xiaobai1226.aether.core.annotation;

import org.noear.solon.annotation.Note;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 图片验证验证码是否正确 注解
 * TODO 待研究怎么指定处理顺序
 *
 * @author bai
 */
//@Target({TYPE, ANNOTATION_TYPE})
@Target({ElementType.METHOD})
@Retention(RUNTIME)
@Documented
public @interface ImageCaptchaVerify {

    @Note("错误提示")
    String message() default "";

    @Note("验证码字段名")
    String captchaCode();

    @Note("验证码ID字段名")
    String captchaId();

//    @Target({TYPE, ANNOTATION_TYPE})
//    @Retention(RUNTIME)
//    @Documented
//    @interface List {
//        ImageCaptchaVerify[] value();
//    }
}
