package com.xiaobai1226.aether.core.constant.result.error;

/**
 * 分享功能相关 返回错误描述枚举类
 *
 * @author bai
 */
public class ResultShareErrMsgConsts {

    /**
     * 分享内容为空错误
     */
    public static final String ERROR_SHARE_CONTENT_EMPTY = "请选择要分享的文件(夹)";

    /**
     * 分享提取码格式错误
     */
    public static final String ERROR_SHARE_EXTRACTION_CODE_FORMAT = "提取码格式不正确，必须为只包含字母或数字的4位字符串";

    /**
     * 有效期为空错误
     */
    public static final String ERROR_SHARE_VALIDITY_PERIOD_EMPTY = "分享有效期不能为空";

    /**
     * 取消分享内容为空错误
     */
    public static final String ERROR_SHARE_CANCEL_CONTENT_EMPTY = "请选择要取消分享的文件(夹)";

    /**
     * 要取消分享的文件(夹)不存在错误
     */
    public static final String ERROR_SHARE_CANCEL_CONTENT_NOT_EXIST = "要取消分享的文件(夹)不存在";
}
