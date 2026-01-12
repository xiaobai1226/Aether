package com.xiaobai1226.aether.core.webdav;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xiaobai1226.aether.core.service.intf.WebDavService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.web.webdav.FileSystem;
import org.noear.solon.web.webdav.WebdavAbstractHandler;

/**
 * webdav抽象拦截器
 *
 * @author 高压锅里的小白
 */
@Component
public class NetdiskWebdavHandler extends WebdavAbstractHandler {

    @Inject
    private WebDavService webDavService;

    @Inject
    private FileSystem fileSystem;

    public NetdiskWebdavHandler() {
        super(true); // 启用 range 支持，用于 HTTP Range 请求
    }

    @Override
    public String user(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        // 截取掉前缀 "Basic "
        String encodedCredentials = authHeader.substring("Basic ".length()).trim();
        // 对编码的字符串进行解码
        String credentials = Base64.decodeStr(encodedCredentials);
        // credentials的格式应该是 "用户名:密码"，所以需要继续解析
        final String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = URLUtil.decode(values[1]);

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return null;
        }

        // 验证用户名和密码，并获取用户ID
        String userIdStr = webDavService.checkUsernameAndPassword(username, password);

        if (userIdStr != null) {
            // 将userId存入ThreadLocal供FileSystem使用
            Long userId = Long.parseLong(userIdStr);
            UserContext.setUserId(userId);
        }

        return userIdStr;
    }

    @Override
    public FileSystem fileSystem() {
        return fileSystem;
    }

    @Override
    public String prefix() {
        return "webdav";
    }
}