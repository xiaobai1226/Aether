package com.xiaobai1226.aether.common.exception;

import com.xiaobai1226.aether.common.enums.ResultCodeEnum;
import com.xiaobai1226.aether.common.domain.dto.Result;

/**
 * 失败结果异常类
 * TODO 警告待处理
 *
 * @author bai
 */
public class FailResultException extends RuntimeException {

    /**
     * 结果类
     */
    private Result result;

    public FailResultException(ResultCodeEnum resultCode) {
        super();
        this.result = Result.fail(resultCode);
    }

    public FailResultException(ResultCodeEnum resultCode, String msg) {
        super();
        this.result = Result.fail(resultCode, msg);
    }

    public Result getResult() {
        return result;
    }
}
