package com.xiaobai1226.aether.core.config;

import com.xiaobai1226.aether.core.annotation.EnumValid;
import com.xiaobai1226.aether.core.annotation.EnumValidtor;
import com.xiaobai1226.aether.core.annotation.ImageCaptchaVerify;
import com.xiaobai1226.aether.core.annotation.ImageCaptchaVerifyValidator;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.validation.ValidatorManager;

/**
 * 校验配置类
 *
 * @author bai
 */
@Configuration
public class ValidatorConfig {

    @Bean
    public void adapter() {
        // 此处为注册验证器。如果有些验证器重写了，也是在此处注册
        // 枚举校验注解
        ValidatorManager.register(EnumValid.class, new EnumValidtor());
        // 图片验证码校验注解
        ValidatorManager.register(ImageCaptchaVerify.class, new ImageCaptchaVerifyValidator());
    }
}
