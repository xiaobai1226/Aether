package com.xiaobai1226.aether.core;

import com.xiaobai1226.aether.core.util.BannerUtils;
import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.web.cors.CrossFilter;

/**
 * Solon启动类
 *
 * @author bai
 */
@SolonMain
public class AetherCoreApp {

    public static void main(String[] args) {
        Solon.start(AetherCoreApp.class, args, app -> {
            BannerUtils.banner();
            
            // 增加全局处理跨域（用过滤器模式）加-1 优先级更高
            app.filter(-1, new CrossFilter().allowedOrigins("*"));
        });
    }
}
