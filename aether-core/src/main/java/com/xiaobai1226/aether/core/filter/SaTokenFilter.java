package com.xiaobai1226.aether.core.filter;

import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

/**
 * SaToken过滤器
 */
public class SaTokenFilter implements Filter {
    /**
     * 无需登录即可访问的地址
     */
    private final String[] excludePathArray = new String[]{
            // 验证码相关
            "/api/v1/captcha/**",
            // 登录
            "/api/v1/login",
            // 注册
            "/api/v1/register",
            // 重置密码
            "/api/v1/resetPassword",
            // 获取分享信息
            "/api/v1/share/getShareInfo",
            // 获取分享信息
            "/api/v1/share/getShareLoginInfo",
            // 校验分享码
            "/api/v1/share/checkExtractionCode",
            // 获取分享文件详情信息
            "/api/v1/share/getShareFileListByShareIdPagination",

            // 下载
            "/api/v1/file/download"};

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        SaRouter.match("/**").notMatch(excludePathArray).check(StpUtil::checkLogin);

        chain.doFilter(ctx);
    }
}