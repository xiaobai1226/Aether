package com.xiaobai1226.aether.core.service.intf;

/**
 * WebDAV接口
 *
 * @author bai
 */
public interface WebDavService {

    /**
     * 校验用户名密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户Id
     * @author bai
     */
    Integer checkUsernameAndPassword(String username, String password);
}
