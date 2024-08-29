//package com.xiaobai1226.aether.core.config;
//
//import cn.dev33.satoken.router.SaRouter;
//import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
//import cn.dev33.satoken.stp.StpUtil;
//import org.noear.solon.annotation.Bean;
//import org.noear.solon.annotation.Configuration;
//
///**
// * SaToken配置类
// *
// * @author bai
// */
//@Configuration
//public class SaTokenConfig {
//
//    /**
//     * 无需登录即可访问的地址
//     */
//    private final String[] excludePathArray = new String[]{
//            // 验证码相关
//            "/captcha/**",
//            // 登录
//            "/login",
//            // 注册
//            "/register",
//            // 重置密码
//            "/resetPassword",
//            // 获取分享信息
//            "/share/getShareInfo",
//            // 获取分享信息
//            "/share/getShareLoginInfo",
//            // 校验分享码
//            "/share/checkExtractionCode",
//            // 获取分享文件详情信息
//            "/share/getShareFileListByShareIdPagination",
//
//            // 下载
//            "/file/download"};
//
//    @Bean(index = -100) //-100，是顺序位（低值优先）
//    public SaTokenInterceptor saTokenInterceptor() {
//        //用于支持规划处理及注解处理
//        return new SaTokenInterceptor()
//                // 指定 [拦截路由] 与 [放行路由]
//                .addInclude("/api/v1/**").addExclude(excludePathArray)
//
//                // 认证函数: 每次请求执行
//                .setAuth(req -> {
//                    SaRouter.match("/api/v1/**", StpUtil::checkLogin);
//                });
//    }
//}
