package com.xiaobai1226.aether.core.webdav;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xiaobai1226.aether.core.service.intf.WebDavService;
import com.xiaobai1226.aether.core.webdav.intf.FileSystem;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;

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

//    public NetdiskWebdavHandler() {
//
//    }
//
//    public NetdiskWebdavHandler(boolean range) {
//        super(range);
//    }

    @Override
    public Integer user(Context ctx) {
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

        // 你现在可以使用username和password做进一步的处理
        return webDavService.checkUsernameAndPassword(username, password);
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
