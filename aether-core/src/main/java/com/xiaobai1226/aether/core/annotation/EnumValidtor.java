package com.xiaobai1226.aether.core.annotation;

import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.Validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 校验入参是否为指定enum的值的实现方法
 *
 * @author bai
 */
public class EnumValidtor implements Validator<EnumValid> {

    @Override
    public String message(EnumValid anno) {
        return anno.message();
    }

    /**
     * 校验强类型值（注解在参数上时用到）
     */
    @Override
    public Result validateOfValue(EnumValid anno, Object val, StringBuilder tmp) {
        if (val != null && !(val instanceof String)) {
            return Result.failure();
        }

        String value = (String) val;

        if (!verify(anno, value)) {
            return Result.failure();
        } else {
            return Result.succeed();
        }
    }

    /**
     * 校验上下文的参数（注解在函数上时用到）
     */
    @Override
    public Result validateOfContext(Context ctx, EnumValid anno, String name, StringBuilder tmp) {
        String val = ctx.param(name);

        if (!verify(anno, val)) {
            return Result.failure(name);
        } else {
            return Result.succeed();
        }
    }

    /**
     * 校验方法
     *
     * @param anno
     * @param value
     * @return 校验结果
     */
    private boolean verify(EnumValid anno, String value) {
        // 校验字段
        String vaildField = anno.vaildField();
        // 目标枚举类
        Class<?>[] cls = anno.target();

        // TODO 用hutool重写
        if (value != null && !value.isEmpty() && cls.length > 0) {
            for (Class<?> cl : cls) {
                try {
                    if (cl.isEnum()) {
                        //枚举类验证
                        Object[] objs = cl.getEnumConstants();

                        Method codeMethod = cl.getMethod(vaildField);
                        for (Object obj : objs) {
                            Object code = codeMethod.invoke(obj);
                            if (value.equals(code.toString())) {
                                return true;
                            }
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // TODO 修改打印方式
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return true;
        }
        return false;
    }
}