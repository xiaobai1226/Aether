package com.xiaobai1226.aether.core.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import com.xiaobai1226.aether.core.service.intf.WebDavService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import static com.xiaobai1226.aether.core.enums.UserStatusEnum.BAN;

/**
 * 账户Service实现类
 *
 * @author bai
 */
@Component
public class WebDavServiceImpl implements WebDavService {

    @Inject("${sa-token.token-prefix}")
    private String tokenPrefix;

    @Inject
    private UserService userService;

    @Override
    public Integer checkUsernameAndPassword(String username, String password) {
        // 根据用户名获取用户信息
        var userDO = userService.getUserByUsername(username);

        // 用户名不存在或密码错误
        if (userDO == null || !BCrypt.checkpw(password, userDO.getUserPassword())) {
            return null;
        }

        // 账号状态错误
        if (BAN.flag().equals(userDO.getUserStatus())) {
            return null;
        }

        return userDO.getId();
    }
}
