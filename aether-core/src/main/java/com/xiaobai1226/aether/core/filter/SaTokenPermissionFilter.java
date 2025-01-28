package com.xiaobai1226.aether.core.filter;

import cn.dev33.satoken.stp.StpUtil;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

/**
 * SaToken权限过滤器
 */
public class SaTokenPermissionFilter implements Filter {

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        StpUtil.checkRole("admin");
        chain.doFilter(ctx);
    }
}