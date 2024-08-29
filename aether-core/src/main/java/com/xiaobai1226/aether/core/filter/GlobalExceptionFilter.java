package com.xiaobai1226.aether.core.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.http.HttpStatus;
import com.xiaobai1226.aether.core.enums.ResultCodeEnum;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.util.Result;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.validation.ValidatorException;

/**
 * 全局异常处理
 *
 * @author bai
 */
@Component
public class GlobalExceptionFilter implements Filter {
    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        try {
            chain.doFilter(ctx);

            if (!ctx.getHandled()) {
                // 404异常处理
                ctx.render(Result.fail(ResultCodeEnum.NOT_FOUND_ERROR));
            }
        } catch (ValidatorException e) { // 校验异常
            ctx.status(HttpStatus.HTTP_BAD_REQUEST);
            ctx.render(Result.fail(ResultCodeEnum.PARAM_IS_INVALID, e.getMessage()));
        } catch (FailResultException e) { // 自定义错误结果异常
            ctx.status(HttpStatus.HTTP_BAD_REQUEST);
            ctx.render(e.getResult());
        } catch (NotLoginException e) { // 未登录异常
            ctx.status(HttpStatus.HTTP_UNAUTHORIZED);
            // 判断场景值，定制化异常信息
            if (e.getType().equals(NotLoginException.TOKEN_TIMEOUT)) { // token超时
                ctx.render(Result.fail(ResultCodeEnum.UNAUTHORIZED_TOKEN_TIMEOUT_ERROR));
            } else { // 未登录
                ctx.render(Result.fail(ResultCodeEnum.UNAUTHORIZED_ERROR));
            }
        } catch (StatusException e) {
            // 如果是404交给前端处理
            if (e.getCode() == HttpStatus.HTTP_NOT_FOUND) {
                ctx.forward("/");
            }
        } catch (Throwable e) { // 其他异常错误
            ctx.status(HttpStatus.HTTP_INTERNAL_ERROR);
            ctx.render(Result.fail(ResultCodeEnum.SYSTEM_ERROR));
        }
    }
}

///**
// * json参数校验异常
// *
// * @param e 处理 json 请求体调用接口校验失败抛出的异常
// * @author bai
// * @date 2022/09/17 23:17
// */
//@ExceptionHandler({MethodArgumentNotValidException.class})
//@ResponseStatus(HttpStatus.BAD_REQUEST)
//private Result methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
//    StringBuilder errMsgBuilder = new StringBuilder();
//
//    e.getBindingResult().getAllErrors().forEach((objectError) -> {
//        if (objectError instanceof FieldError fieldError) {
//            //Field 上的 FieldError 类型错误
//            errMsgBuilder.append(fieldError.getDefaultMessage());
//        } else {
//            //Class 上的 ViolationFieldError 类型错误
//            errMsgBuilder.append(objectError.getDefaultMessage());
//        }
//    });
//
//    return Result.fail(ResultCodeEnum.PARAM_IS_INVALID, errMsgBuilder.toString());
//}
//
///**
// * form data方式参数校验异常
// *
// * @param e 处理 form data方式调用接口校验失败抛出的异常
// * @author bai
// */
//@ExceptionHandler({BindException.class})
//@ResponseStatus(HttpStatus.BAD_REQUEST)
//private Result bindExceptionHandler(BindException e) {
//    StringBuilder errMsgBuilder = new StringBuilder();
//
//    e.getBindingResult().getAllErrors().forEach((objectError) -> {
//        if (objectError instanceof FieldError fieldError) {
//            //Field 上的 FieldError 类型错误
//            errMsgBuilder.append(fieldError.getDefaultMessage());
//        } else {
//            //Class 上的 ViolationFieldError 类型错误
//            errMsgBuilder.append(objectError.getDefaultMessage());
//        }
//    });
//
//    return Result.fail(ResultCodeEnum.PARAM_IS_INVALID, errMsgBuilder.toString());
//}
//
///**
// * 处理单个参数校验失败抛出的异常
// *
// * @param e 处理单个参数校验失败抛出的异常
// * @author bai
// */
//@ExceptionHandler(ConstraintViolationException.class)
//@ResponseStatus(HttpStatus.BAD_REQUEST)
//public Result constraintViolationExceptionHandler(ConstraintViolationException e) {
//    Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
//    List<String> collect = constraintViolations.stream().map(o -> o.getMessage()).collect(Collectors.toList());
//    return Result.fail(ResultCodeEnum.PARAM_IS_INVALID, collect.get(0));
//}