package com.xiaobai1226.aether.core.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.util.ResourceUtil;

/**
 * Banner打印工具类
 */
@Component
@Slf4j
public class BannerUtils {

    /**
     * 打印banner
     */
    public static void banner() {
        try {
            var hasBanner = ResourceUtil.hasResource("banner.txt");
            if (!hasBanner) {
                return;
            }

            String bannerTxt = ResourceUtil.getResourceAsString("banner.txt");

            if (StrUtil.isBlank(bannerTxt)) {
                return;
            }

            bannerTxt = bannerTxt.replace("${solon.app.version}", Solon.cfg().get("solon.app.version", ""));

            log.info(bannerTxt);
        } catch (Exception e) {
        }
    }
}
