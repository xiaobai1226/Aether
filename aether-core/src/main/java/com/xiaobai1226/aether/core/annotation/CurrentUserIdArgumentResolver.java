package com.xiaobai1226.aether.core.annotation;

import cn.dev33.satoken.stp.StpUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodArgumentResolver;
import org.noear.solon.core.util.LazyReference;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.core.wrap.ParamWrap;

/**
 * 当前登录用户ID参数解析器（Solon版本）
 * 自动将当前登录用户的ID注入到Controller方法的参数中
 *
 * @author bai
 */
@Component
public class CurrentUserIdArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否匹配该参数
     * 只有当参数上标注了 @CurrentUserId 注解时才进行解析
     *
     * @param ctx   请求上下文
     * @param pWrap 参数包装器
     * @return 是否匹配该参数
     */
    @Override
    public boolean matched(Context ctx, ParamWrap pWrap) {
        // 检查参数是否带有 @CurrentUserId 注解
        return pWrap.getParameter().isAnnotationPresent(CurrentUserId.class);
    }

    /**
     * 解析参数值
     * 从 Sa-Token 中获取当前登录用户的ID并返回
     *
     * @param ctx     请求上下文
     * @param target  控制器
     * @param mWrap   函数包装器
     * @param pWrap   参数包装器
     * @param pIndex  参数序位
     * @param bodyRef 主体引用
     * @return 当前登录用户的ID
     * @throws Throwable 解析异常
     */
    @Override
    public Object resolveArgument(Context ctx, Object target, MethodWrap mWrap, ParamWrap pWrap, int pIndex,
            LazyReference bodyRef) throws Throwable {
        // 从 Sa-Token 中获取当前登录用户的ID
        return StpUtil.getLoginIdAsLong();
    }
}