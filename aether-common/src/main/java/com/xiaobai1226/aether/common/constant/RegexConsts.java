package com.xiaobai1226.aether.common.constant;

import static com.xiaobai1226.aether.common.constant.VerifyConsts.*;

/**
 * 校验正则表达式常量类
 *
 * @author bai
 */
public class RegexConsts {

    /**
     * 密码正则表达式
     */
    public static final String REGEX_PASSWARD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()<>?]).{" + PASSWORD_MIN_LENGTH + "," + PASSWORD_MAX_LENGTH + "}$";

    /**
     * 用户名正则表达式
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_-]{" + (USERNAME_MIN_LENGTH - 1) + "," + (USERNAME_MAX_LENGTH - 1) + "}$";

    /**
     * 文件或文件夹名称正则表达式
     */
    public static final String REGEX_FILE_NAME = "^[^<>\\|\\*\\?/]*$";

    /**
     * 提取码正则表达式
     */
    public static final String REGEX_EXTRACTION_CODE = "^[a-zA-Z0-9]{4}$";
}
