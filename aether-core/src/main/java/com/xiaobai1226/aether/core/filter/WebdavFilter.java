package com.xiaobai1226.aether.core.filter;

import com.xiaobai1226.aether.core.webdav.NetdiskWebdavHandler;
import com.xiaobai1226.aether.core.webdav.UserContext;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

@Component(index = -10000)
public class WebdavFilter implements Filter {

    @Inject
    private NetdiskWebdavHandler handler;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (ctx.path().startsWith("/webdav")) {
            try {
                // 自己实现处理
                handler.handle(ctx);
                ctx.setHandled(true);
            } finally {
                // 清理 ThreadLocal，防止内存泄漏
                UserContext.clear();
            }
        } else {
            chain.doFilter(ctx);
        }
    }
}