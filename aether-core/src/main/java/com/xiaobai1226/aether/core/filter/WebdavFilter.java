package com.xiaobai1226.aether.core.filter;

import com.xiaobai1226.aether.core.webdav.NetdiskWebdavHandler;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

@Component(index = -1)
public class WebdavFilter implements Filter {

    @Inject
    private NetdiskWebdavHandler handler;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (ctx.path().startsWith("/webdav")) {
            //自己实现处理
            handler.handle(ctx);
        } else {
            chain.doFilter(ctx);
        }
    }
}