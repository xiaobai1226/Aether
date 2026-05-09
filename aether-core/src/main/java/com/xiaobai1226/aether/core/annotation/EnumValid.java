package com.xiaobai1226.aether.core.annotation;

import org.noear.solon.annotation.Note;

import java.lang.annotation.*;

/**
 * 验证是否在枚举范围内
 *
 * @author bai
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumValid {

    @Note("错误提示")
    String message() default "";

    @Note("目标枚举类")
    Class<?>[] target() default {};

    @Note("需要校验的字段")
    String vaildField() default "";

    @Note("是否允许为null，默认为false（保持原有逻辑）")
    boolean allowNull() default false;
}