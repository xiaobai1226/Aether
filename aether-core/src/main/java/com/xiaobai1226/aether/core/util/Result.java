package com.xiaobai1226.aether.core.util;

import com.xiaobai1226.aether.core.enums.ResultCodeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果工具类
 *
 * @author bai
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private Integer code;

    private String msg;

    private T data;

    public Result(ResultCodeEnum resultCode, T data) {
        this.code = resultCode.code();
        this.msg = resultCode.msg();
        this.data = data;
    }

    private void setResultCode(ResultCodeEnum resultCode) {
        this.code = resultCode.code();
        this.msg = resultCode.msg();
    }

    // 返回成功
    public static <T> Result<T> success() {
        var result = new Result<T>();
        result.setResultCode(ResultCodeEnum.SUCCESS);
        return result;
    }

    // 返回成功
    public static <T> Result<T> success(String msg) {
        var result = new Result<T>();
        result.setCode(ResultCodeEnum.SUCCESS.code());
        result.setMsg(msg);
        return result;
    }

    // 返回成功
    public static <T> Result<T> success(T data) {
        var result = new Result<T>();
        result.setResultCode(ResultCodeEnum.SUCCESS);
        result.setData(data);
        return result;
    }

    // 返回成功
    public static <T> Result<T> success(String msg, T data) {
        var result = new Result<T>();
        result.setCode(ResultCodeEnum.SUCCESS.code());
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 返回失败
    public static <T> Result<T> fail(Integer code, String msg) {
        var result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 返回失败
    public static <T> Result<T> fail(ResultCodeEnum resultCode) {
        var result = new Result<T>();
        result.setResultCode(resultCode);
        return result;
    }

    // 返回失败
    public static <T> Result<T> fail(ResultCodeEnum resultCode, String msg) {
        var result = new Result<T>();
        result.setResultCode(resultCode);
        result.setMsg(msg);
        return result;
    }
}
