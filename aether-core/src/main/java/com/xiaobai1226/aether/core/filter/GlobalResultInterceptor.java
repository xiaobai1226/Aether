package com.xiaobai1226.aether.core.filter;

import com.xiaobai1226.aether.common.domain.dto.Result;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;

import java.util.Objects;

/**
 * TODO 重写，是否有问题要确认
 * 统一返回结果
 *
 * @author bai
 */
@Component
public class GlobalResultInterceptor implements RouterInterceptor {

    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        //提示：这里和 doFilter 差不多...
        chain.doIntercept(ctx, mainHandler);
    }

    /**
     * 提交结果（ render 执行前调用）//不要做太复杂的事情
     */
    @Override
    public Object postResult(Context ctx, Object result) throws Throwable {
        Result finalResult;

        //如果返回值类型为void，则默认返回成功
        if (result instanceof Throwable) {
            //异常类型，根据需要处理
            return result;
        } else if (result instanceof Void) {
            finalResult = Result.success();
        } else if (result instanceof Result resultData) {
            finalResult = resultData;
        } else if (result instanceof String) {
            //json的转换会出问题
            finalResult = Result.success(result);
        } else if (result instanceof ModelAndView) {
            return result;
        } else {
            if (Objects.isNull(result)) {
                finalResult = Result.success();
            } else {
                finalResult = Result.success(result);
            }
        }

        //一定要转换为String，否则会报错
        return finalResult;
    }
}
