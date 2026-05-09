package com.xiaobai1226.aether.core.annotation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.Validator;

/**
 * 校验入参是否为指定enum的值的实现方法
 *
 * @author bai
 */
@Slf4j
public class EnumValidtor implements Validator<EnumValid> {

    @Override
    public String message(EnumValid anno) {
        return anno.message();
    }

    /**
     * 校验强类型值（注解在参数上时用到）
     */
    @Override
    public Result<Void> validateOfValue(EnumValid anno, Object val, StringBuilder tmp) {
        // 如果值为 null，根据 allowNull 参数决定是否允许
        if (val == null) {
            return anno.allowNull() ? Result.succeed() : Result.failure();
        }

        if (!verify(anno, val)) {
            return Result.failure();
        }

        return Result.succeed();
    }

    /**
     * 校验上下文的参数（注解在函数上时用到）
     */
    @Override
    public Result<Void> validateOfContext(Context ctx, EnumValid anno, String name, StringBuilder tmp) {
        String val = ctx.param(name);

        // 如果值为 null 或空字符串，根据 allowNull 参数决定是否允许
        if (ObjectUtil.isEmpty(val)) {
            return anno.allowNull() ? Result.succeed() : Result.failure(name);
        }

        if (!verify(anno, val)) {
            return Result.failure(name);
        }

        return Result.succeed();
    }

    /**
     * 校验方法
     *
     * @param anno  注解对象
     * @param value 需要校验的值
     * @return 校验结果
     */
    private boolean verify(EnumValid anno, Object value) {
        // 校验字段
        String validField = anno.vaildField();
        // 目标枚举类
        Class<?>[] cls = anno.target();

        // 如果值为空或没有指定枚举类，返回 true（允许通过）
        if (ObjectUtil.isEmpty(value) || cls.length == 0) {
            return true;
        }

        // 遍历所有指定的枚举类
        for (Class<?> cl : cls) {
            try {
                if (cl.isEnum()) {
                    // 获取枚举类的所有常量
                    Object[] enumConstants = cl.getEnumConstants();

                    // 遍历枚举常量，比对指定字段的值
                    for (Object enumConstant : enumConstants) {
                        // 使用 hutool 的反射工具调用指定字段的 getter 方法
                        Object fieldValue = ReflectUtil.invoke(enumConstant, validField);

                        // 使用 hutool 的对象工具比较值
                        if (ObjectUtil.equal(value, fieldValue) ||
                                ObjectUtil.equal(value.toString(), fieldValue.toString())) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("枚举验证失败，枚举类：{}，校验字段：{}，错误信息：{}",
                        cl.getName(), validField, e.getMessage());
            }
        }

        return false;
    }
}