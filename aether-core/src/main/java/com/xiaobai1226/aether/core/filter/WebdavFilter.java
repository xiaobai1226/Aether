package com.xiaobai1226.aether.core.filter;

import com.xiaobai1226.aether.core.webdav.NetdiskWebdavHandler;
import com.xiaobai1226.aether.core.webdav.UserContext;

import lombok.extern.slf4j.Slf4j;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

/**
 * WebDAV 过滤器
 * 
 * 负责拦截所有 /webdav 路径的请求，交由 NetdiskWebdavHandler 处理
 * 
 * @author 高压锅里的小白
 */
@Component(index = -10000)
@Slf4j
public class WebdavFilter implements Filter {

    @Inject
    private NetdiskWebdavHandler handler;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (ctx.path().startsWith("/webdav")) {
            long startTime = System.currentTimeMillis();
            try {
                log.info("WebDAV 请求开始: method={}, path={}, authorization={}", ctx.method(), ctx.path(),
                        ctx.header("Authorization"));
                
                // 交由 handler 处理（NetdiskWebdavHandler 已经重写了 PROPFIND 逻辑）
                handler.handle(ctx);
                ctx.setHandled(true);
                
            } finally {
                // 清理 ThreadLocal，防止内存泄漏
                UserContext.clear();
                log.info("WebDAV 请求结束: 耗时={}ms, method={}, path={}, status={}, authorization={}",
                        System.currentTimeMillis() - startTime,
                        ctx.method(), ctx.path(), ctx.status(), ctx.header("Authorization"));
            }
        } else {
            chain.doFilter(ctx);
        }
    }
}