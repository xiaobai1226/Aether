package com.xiaobai1226.aether.core.webdav;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xiaobai1226.aether.core.service.intf.WebDavService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import com.xiaobai1226.aether.webdav.FileInfo;
import com.xiaobai1226.aether.webdav.FileSystem;
import com.xiaobai1226.aether.webdav.WebdavAbstractHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WebDAV 抽象处理器实现
 * 
 * 这是 WebDAV 请求的入口处理器，负责处理 HTTP 请求并协调整个 WebDAV 流程。
 * 每次 WebDAV 请求到来时，首先会调用 user() 方法进行用户认证，然后通过 fileSystem()
 * 获取文件系统实现来执行具体的文件操作。
 * 
 * 重写 handle() 方法以修复 macOS Finder 兼容性问题：
 * - 为 href 添加前导斜杠（绝对路径）
 * - 为目录的 href 添加尾部斜杠
 *
 * @author 高压锅里的小白
 */
@Component
@Slf4j
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

    /**
     * 重写 handle 方法以修复 PROPFIND 响应中的 href 格式
     * 
     * macOS Finder 对 WebDAV 协议实现严格，要求：
     * 1. href 必须使用绝对路径（以 / 开头）
     * 2. 目录的 href 必须以 / 结尾
     * 
     * 原始框架生成的 href 格式不符合要求，导致 Finder 将当前目录识别为子项
     * 
     * 策略：对于 PROPFIND 请求，完全重写处理逻辑，生成修复后的 XML
     * 
     * @param ctx HTTP 请求上下文
     */
    @Override
    public void handle(Context ctx) {
        // 对于 PROPFIND 请求，自己实现逻辑以修复 href 格式
        if ("PROPFIND".equals(ctx.method())) {
            try {
                // 设置通用的 WebDAV 响应头（与父类保持一致）
                ctx.contentType("text/xml; charset=UTF-8");
                ctx.headerSet("Pragma", "no-cache");
                ctx.headerSet("Cache-Control", "no-cache");
                ctx.headerSet("X-DAV-BY", "webos");
                ctx.headerSet("Access-Control-Allow-Origin", "*");
                ctx.headerSet("Access-Control-Allow-Methods",
                        "GET, POST, OPTIONS, DELETE, HEAD, MOVE, COPY, PUT, MKCOL, PROPFIND, PROPPATCH, LOCK, UNLOCK");
                ctx.headerSet("Access-Control-Allow-Headers",
                        "ETag, Content-Type, Content-Length, Accept-Encoding, X-Requested-with, Origin, Authorization");
                ctx.headerSet("Access-Control-Allow-Credentials", "true");
                ctx.headerSet("Access-Control-Max-Age", "3600");

                // 用户认证
                if (StrUtil.isBlank(this.user(ctx))) {
                    ctx.headerSet("WWW-Authenticate", "Basic realm=\"webos\"");
                    ctx.status(401);
                    return;
                }

                // 处理 PROPFIND 请求
                int status = handlePropfindFixed(ctx);
                ctx.status(status);

            } catch (Exception e) {
                log.error("处理 PROPFIND 请求失败", e);
                ctx.status(500);
            }
        } else {
            // 其他请求调用父类处理
            super.handle(ctx);
        }
    }

    /**
     * 处理 PROPFIND 请求（修复后的版本）
     * 
     * 复制父类逻辑，但生成修复后的 href（绝对路径 + 目录尾部斜杠）
     * 
     * @param ctx HTTP 请求上下文
     * @return HTTP 状态码
     */
    private int handlePropfindFixed(Context ctx) throws Exception {
        String reqPath = this.stripPrefix(ctx.path());
        FileInfo fi = this.fileSystem().fileInfo(reqPath);
        if (fi == null) {
            return 404;
        }

        // 解析 Depth 头
        int depth = -1;
        String hdr = ctx.header("Depth");
        if (StrUtil.isNotBlank(hdr)) {
            depth = parseDepth(hdr);
            if (depth == -2) {
                return 400;
            }
        }
        if (depth == -1) {
            depth = 1;
        }

        // 生成修复后的响应
        String itemResponse = toItemResponseFixed(reqPath, fi);
        if (!fi.isDir() || depth == 0) {
            ctx.output(toItemListResponse(itemResponse));
            return 207;
        }

        // 列出子文件/文件夹
        List<FileInfo> childs = this.fileSystem().fileList(reqPath);
        List<String> list = CollUtil.newArrayList(itemResponse);
        if (CollUtil.isNotEmpty(childs)) {
            for (FileInfo info : childs) {
                String tmp = StrUtil.isBlank(reqPath) ? info.name() : reqPath + "/" + info.name();
                list.add(toItemResponseFixed(tmp, info));
            }
        }
        String out = toItemListResponse(ArrayUtil.toArray(list, String.class));
        ctx.output(out);
        return 207;
    }

    /**
     * 生成单个文件/文件夹的响应（修复后的版本）
     * 
     * 与父类不同之处：
     * 1. href 使用绝对路径（添加前导 /）
     * 2. 目录的 href 添加尾部 /
     * 
     * @param reqPath 请求路径
     * @param fi      文件信息
     * @return XML 响应片段
     */
    private String toItemResponseFixed(String reqPath, FileInfo fi) {
        String template = "<D:response>\n" +
                "<D:href>{}</D:href>\n" +
                "<D:propstat>\n" +
                "\t<D:prop>\n" +
                "\t\t<D:getlastmodified>{}</D:getlastmodified>\n" +
                "\t\t<D:creationdate>{}</D:creationdate>\n" +
                "\t\t<D:getcontentlength>{}</D:getcontentlength>\n" +
                "\t\t<D:resourcetype>{}</D:resourcetype><D:getcontenttype>{}</D:getcontenttype>\n" +
                "\t</D:prop>\n" +
                "\t<D:status>HTTP/1.1 200 OK</D:status>\n" +
                "</D:propstat>\n" +
                "\t</D:response>";

        // 生成 href：添加前导 / 和尾部 / （如果是目录）
        String href;
        if (StrUtil.isBlank(reqPath)) {
            // 根目录
            href = "/" + this.prefix() + "/";
        } else {
            // 子路径
            href = "/" + this.prefix() + "/" + reqPath;
            // 如果是目录，添加尾部 /
            if (fi.isDir() && !href.endsWith("/")) {
                href = href + "/";
            }
        }

        // URL 编码
        href = encodePath(href);

        return StrUtil.format(template,
                href,
                DateUtil.parse(fi.update()).toJdkDate().toString(),
                DateUtil.parse(fi.create()).toString("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                fi.size(),
                fi.isDir() ? "<D:collection/>" : "",
                this.fileSystem().fileMime(fi));
    }

    /**
     * 包装多个响应项为完整的 XML
     * 
     * @param itemResponses 响应项数组
     * @return 完整的 XML 响应
     */
    private String toItemListResponse(String... itemResponses) {
        String template = "<D:multistatus xmlns:D=\"DAV:\"> \n" +
                "\t{}\n" +
                "</D:multistatus>";
        StringBuilder sb = new StringBuilder();
        for (String tmp : itemResponses) {
            sb.append(tmp);
        }
        return StrUtil.format(template, sb.toString());
    }

    /**
     * 解析 Depth 头
     * 
     * @param s Depth 头的值
     * @return 深度值：0, 1, -1(infinity), -2(invalid)
     */
    private int parseDepth(String s) {
        switch (s) {
            case "0":
                return 0;
            case "1":
                return 1;
            case "infinity":
                return -1;
        }
        return -2;
    }

    /**
     * 去除路径前缀
     * 
     * @param p 完整路径
     * @return 去除前缀后的路径
     */
    private String stripPrefix(String p) {
        p = decodePath(p);
        int index = p.indexOf(this.prefix());
        if (index == -1) {
            return "";
        }
        String r = p.substring(index + this.prefix().length());
        if (r.length() < p.length()) {
            if (r.endsWith("/")) {
                r = r.substring(0, r.length() - 1);
            }
            if (r.startsWith("/")) {
                r = r.substring(1);
            }
            return r;
        }
        return "";
    }

    private String decodePath(String path) {
        if (path == null) {
            return null;
        }

        return URLUtil.decode(path.replace("+", "%2B"));
    }

    private String encodePath(String path) {
        if (path == null) {
            return null;
        }

        return Arrays.stream(path.split("/", -1))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }
}
