package com.xiaobai1226.aether.core.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.xiaobai1226.aether.core.domain.dto.LoginUserInfoDTO;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.AccountService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_ACCOUNT_BAN;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_ACCOUNT_OR_PASSWORD;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserStatusEnum.BAN;

/**
 * 账户Service实现类
 *
 * @author bai
 */
@Component
public class AccountServiceImpl implements AccountService {

    @Inject("${sa-token.token-prefix}")
    private String tokenPrefix;

    @Inject
    private UserService userService;

    @Override
    public LoginUserInfoDTO login(String username, String password) {
        // 根据用户名获取用户信息
        var userDO = userService.getUserByUsername(username);

        // 用户名密码错误
        if (userDO == null || !BCrypt.checkpw(password, userDO.getUserPassword())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_ACCOUNT_OR_PASSWORD);
        }

        // 账号状态错误
        if (BAN.flag().equals(userDO.getUserStatus())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_ACCOUNT_BAN);
        }

        StpUtil.login(userDO.getId());
        var tokenInfo = StpUtil.getTokenInfo();

        // 更新登录时间
        userService.updateUserLastLoginTime(userDO.getId());

        var loginUserInfoDTO = BeanUtil.toBean(userDO, LoginUserInfoDTO.class);
        loginUserInfoDTO.setToken(tokenInfo.getTokenValue());
        loginUserInfoDTO.setTokenName(tokenInfo.getTokenName());
        loginUserInfoDTO.setTokenPrefix(tokenPrefix);

        return loginUserInfoDTO;
    }
}
