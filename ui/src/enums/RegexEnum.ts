import {VerifyEnum} from "@/enums/VerifyEnum";

/**
 * 正则表达式枚举
 */
export enum RegexEnum {
    /**
     * 密码正则表达式
     */
    REGEX_PASSWARD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()<>?]).{" + VerifyEnum.PASSWORD_MIN_LENGTH + "," + VerifyEnum.PASSWORD_MAX_LENGTH + "}$",

    /**
     * 用户名正则表达式
     */
    REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_-]{" + (VerifyEnum.USERNAME_MIN_LENGTH - 1) + "," + (VerifyEnum.USERNAME_MAX_LENGTH - 1) + "}$",

    /**
     * 文件或文件夹名称正则表达式
     */
    REGEX_FILE_NAME = "^[^<>|*?,/]*$",

    /**
     * 提取码正则表达式
     */
    REGEX_EXTRACTION_CODE = "^[a-zA-Z0-9]{4}$"
}