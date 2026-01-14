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
 * WebDAV 抽象处理器实现
 * 
 * 这是 WebDAV 请求的入口处理器，负责处理 HTTP 请求并协调整个 WebDAV 流程。
 * 每次 WebDAV 请求到来时，首先会调用 user() 方法进行用户认证，然后通过 fileSystem()
 * 获取文件系统实现来执行具体的文件操作。
 *
 * @author 高压锅里的小白
 */
@Component
public class NetdiskWebdavHandler extends WebdavAbstractHandler {

    @Inject
    private WebDavService webDavService;

    @Inject
    private FileSystem fileSystem;

    /**
     * 构造函数
     * 
     * @param enableRange true 表示启用 HTTP Range 请求支持，用于断点续传功能
     */
    public NetdiskWebdavHandler() {
        super(true); // 启用 range 支持，用于 HTTP Range 请求
    }

    /**
     * 用户认证方法
     * 
     * 从 HTTP 请求头中提取并验证用户身份。这是 WebDAV 请求处理的第一步，
     * 每次请求都会首先调用此方法进行认证。
     * 
     * 调用场景：
     * Windows 资源管理器连接 WebDAV 时会发送 Basic Auth 认证
     * macOS Finder 挂载网络磁盘时也会发送认证信息
     * 任何 WebDAV 客户端首次连接时都需要认证
     * 
     * 认证流程：
     * 从请求头中提取 "Authorization" 字段
     * 解析 Basic Auth 格式的认证信息（Base64 编码的 "用户名:密码"）
     * 验证用户名和密码，获取用户ID
     * 将用户ID存入 ThreadLocal，供后续 FileSystem 方法使用
     * 
     * @param ctx HTTP 请求上下文，包含请求头、参数等信息
     * @return 用户ID字符串，认证失败返回 null
     */
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

    /**
     * 获取文件系统实现
     * 
     * 返回 FileSystem 接口的实现，用于执行具体的文件操作（读取、写入、删除等）。
     * 在用户认证通过后，WebDAV 框架会调用此方法获取文件系统实例。
     * 
     * 调用时机：在认证通过后，需要执行具体文件操作时调用
     * 
     * @return FileSystem 接口的实现实例
     */
    @Override
    public FileSystem fileSystem() {
        return fileSystem;
    }

    /**
     * 获取 WebDAV 服务的 URL 路径前缀
     * 
     * 定义 WebDAV 服务的 URL 路径前缀。例如返回 "webdav" 后，
     * 访问路径就是 http://localhost/webdav/...
     * 
     * 示例：
     * prefix() 返回 "webdav" → 访问路径：/webdav/文件.txt
     * prefix() 返回 "dav" → 访问路径：/dav/文件.txt
     * 
     * @return URL 路径前缀字符串
     */
    @Override
    public String prefix() {
        return "webdav";
    }
}