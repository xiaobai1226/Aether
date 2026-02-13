package com.xiaobai1226.aether.common.enums;

/**
 * 结果值枚举类
 *
 * @author bai
 */
public enum ResultCodeEnum {

    /**
     * 成功状态码
     */
    SUCCESS(200, "成功"),

    BAD_REQUEST_ERROR(400, "请求失败"),

    /**
     * 登录错误码
     */
    UNAUTHORIZED_ERROR(401, "您还未登录，请登录后使用"),
    UNAUTHORIZED_TOKEN_TIMEOUT_ERROR(401, "登录已过期，请重新登录"),
    USER_OR_PASSWORD_ERROR(2001, "用户名或密码不正确"),

    /**
     * 系统错误状态码
     */
    FORBIDDEN_ERROR(403, "您缺少使用此功能的权限"),
    NOT_FOUND_ERROR(404, "您所访问的页面不存在"),
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),

    /**
     * 业务错误状态码
     */
    PARAM_IS_INVALID(1001, "参数输入非法"),
    PARAM_IS_BLANK(1002, "参数为空"),
    UPLOAD_TASK_EXPIRED(2101, "上传任务不存在或已过期"),
    UPLOAD_CHUNK_RANGE_INVALID(2102, "分片索引不合法"),
    UPLOAD_CHUNKS_INCOMPLETE(2103, "分片尚未上传完整"),
    UPLOAD_TASK_CONFLICT(2104, "上传任务参数冲突"),


    PARAM_TYPE_BIND_ERROR(401, "参数类型错误"),
    PARAM_NOT_COMPLETE(402, "参数缺失"),
    USER_NOTLOGGED_IN(501, "用户未登录"),
    USER_LOGIN_ERROR(502, "账号不存在或密码错误");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer code() {
        return this.code;
    }

    public String msg() {
        return this.msg;
    }
}