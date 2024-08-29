package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.core.domain.dto.LoginUserInfoDTO;

/**
 * 账户服务接口
 *
 * @author bai
 */
public interface AccountService {

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录用户信息
     * @author bai
     */
    LoginUserInfoDTO login(String username, String password);
}
