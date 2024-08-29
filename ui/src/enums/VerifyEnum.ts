/**
 * 校验参数枚举
 */
export enum VerifyEnum {

    /**
     * 用户名最大长度
     */
    USERNAME_MAX_LENGTH = 20,

    /**
     * 用户名最小长度
     */
    USERNAME_MIN_LENGTH = 4,

    /**
     * 密码最大长度
     */
    PASSWORD_MAX_LENGTH = 32,

    /**
     * 密码最小长度
     */
    PASSWORD_MIN_LENGTH = 8,

    /**
     * 验证码长度
     */
    CAPTCHA_CODE_LENGTH = 4,

    /**
     * 文件或文件夹最大长度
     */
    FILE_NAME_MAX_LENGTH = 100
}