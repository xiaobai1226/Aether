package com.xiaobai1226.aether.core.annotation;

import java.lang.annotation.*;

/**
 * 当前登录用户ID注解
 * 用于Controller方法参数，自动注入当前登录用户的ID
 *
 * @author bai
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {
}