package com.xiaobai1226.aether.core.annotation;

import org.noear.solon.annotation.Note;

import java.lang.annotation.*;

/**
 * 验证是否在枚举范围内
 *
 * @author bai
 */
@Target({ElementType.FIELD})
//@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumValid {

    @Note("错误提示")
    String message() default "";

    @Note("目标枚举类")
    Class<?>[] target() default {};

    @Note("需要校验的字段")
    String vaildField() default "";
}
