package com.xiaobai1226.aether.core.filter;

import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

import java.util.Set;

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

    /**
     * token在URL中校验登录的接口
     */
    private final Set<String> urlCheckPathSet = Set.of(
            "/api/v1/file/getImage",
            "/api/v1/file/getVideo",
            "/api/v1/file/getFile",
            "/api/v1/file/getThumbnail",
            "/api/v1/user/getAvatar"
    );

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (urlCheckPathSet.contains(ctx.path())) {
            String sign = ctx.param("sign");
            if (sign != null) {
                String token = Base64.decodeStr(sign);
                String[] tokenParam = token.split(":");
                if (tokenParam.length == 2) {
                    ctx.headerMap().add(tokenParam[0], tokenParam[1]);
                }
            }
        }

        SaRouter.match("/**").notMatch(excludePathArray).check(StpUtil::checkLogin);

        chain.doFilter(ctx);
    }
}