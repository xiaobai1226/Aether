/**
 * 登录信息
 */
export interface LoginInfo {
    // 用户名
    username: string;
    // 密码
    password: string;
    // 图形验证码ID
    captchaId: string;
    // 图形验证码
    captchaCode: string;
    // 记住我
    // rememberMe: boolean;
}

/**
 * 登录用户信息
 */
export interface LoginUserInfo {
    /**
     * 昵称
     */
    nickname: string;

    /**
     * 头像
     */
    // avatar: string;

    /**
     * 已使用存储空间 单位 byte
     */
    usedStorage: number;

    /**
     * 总存储空间 单位 byte
     */
    totalStorage: number;

    /**
     * 角色ID
     */
    // roleId: number;

    /**
     * 登录token
     */
    token: string;

    /**
     * 登录token名称
     */
    tokenName: string;

    /**
     * 登录前缀
     */
    tokenPrefix: string;
}