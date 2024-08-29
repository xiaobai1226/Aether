import {VerifyEnum} from "@/enums/VerifyEnum";

/**
 * 返回错误描述枚举
 */
export enum ResultErrorMsgEnum {
    ///////////////////////////////////////////////// 密码 ///////////////////////////////////////////////
    /**
     * 密码为空错误
     */
    ERROR_PASSWARD_EMPTY = "密码不能为空",

    /**
     * 密码格式错误
     */
    ERROR_PASSWARD_FORMAT = "密码格式错误，只允许" + VerifyEnum.PASSWORD_MIN_LENGTH + "到" + VerifyEnum.PASSWORD_MAX_LENGTH + "位，包含小大写字母数字及!@#$%^&*()<>?内的特殊字符",

    /**
     * 确认密码为空错误
     */
    ERROR_REPASSWARD_EMPTY = "请再次输入密码",

    /**
     * 两次密码不一致错误
     */
    ERROR_PASSWARD_NOT_MATCH = "两次密码不一致",

    ///////////////////////////////////////////////// 图形验证码 ///////////////////////////////////////////////
    ERROR_IMAGE_CAPTCHA_ID_EMPTY = "图片验证码ID不能为空",

    /**
     * 图形验证码为空错误
     */
    ERROR_IMAGE_CAPTCHA_EMPTY = "图片验证码不能为空",

    /**
     * 图形验证码格式错误
     */
    ERROR_IMAGE_CAPTCHA_FORMAT = "图片验证码格式错误",

    ///////////////////////////////////////////////// 用户名 ///////////////////////////////////////////////
    /**
     * 用户名为空错误
     */
    ERROR_USERNAME_EMPTY = "用户名不能为空",

    /**
     * 用户名格式错误
     */
    ERROR_USERNAME_FORMAT = "用户名格式错误，只允许" + VerifyEnum.USERNAME_MIN_LENGTH + "到" + VerifyEnum.USERNAME_MAX_LENGTH + "位，以字母开头，包含字母数字-或~",

    ///////////////////////////////////////////////// 文件(夹)相关 ///////////////////////////////////////////////
    /**
     * 文件(夹)名称为空错误
     */
    ERROR_FILE_NAME_EMPTY = "文件(夹)名称不能为空",

    /**
     * 文件(夹)名称格式错误
     */
    ERROR_FILE_NAME_FORMAT = "文件(夹)名称不能包含<,>,|,*,?,,/",

    /**
     * 文件(夹)名称长度错误
     */
    ERROR_FILE_NAME_LENGTH = "文件(夹)名称长度不能超过" + VerifyEnum.FILE_NAME_MAX_LENGTH,

    /**
     * 移动内容为空错误
     */
    ERROR_MOVE_CONTENT_EMPTY = "请选择要移动的文件(夹)",

    /**
     * 文件正在当前目录，无需移动 错误
     */
    ERROR_MOVE_IN_CURRENT_FOLDER = "文件正在当前目录，无需移动",

    /**
     * 复制内容为空错误
     */
    ERROR_COPY_CONTENT_EMPTY = "请选择要复制的文件(夹)",

    /**
     * 文件正在当前目录，无需复制 错误
     */
    ERROR_COPY_IN_CURRENT_FOLDER = "文件正在当前目录，无需复制",

    /**
     * 删除内容为空错误
     */
    ERROR_DEL_CONTENT_EMPTY = "请选择要删除的文件(夹)",

    /**
     * 分享内容为空错误
     */
    ERROR_SHARE_CONTENT_EMPTY = "请选择要分享的文件(夹)",

    /**
     * 提取码类型为空错误
     */
    ERROR_SHARE_EXTRACTION_CODE_TYPE_EMPTY = "请选择提取码类型",

    /**
     * 提取码为空错误
     */
    ERROR_SHARE_EXTRACTION_CODE_EMPTY = "请选择提取码",

    /**
     * 分享提取码格式错误
     */
    ERROR_SHARE_EXTRACTION_CODE_FORMAT = "提取码格式不正确，必须为只包含字母或数字的4位字符串",

    /**
     * 有效期为空错误
     */
    ERROR_SHARE_VALIDITY_PERIOD_EMPTY = "分享有效期不能为空"
}