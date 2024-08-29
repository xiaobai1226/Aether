package com.xiaobai1226.aether.core.config;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RedissonClient;
import org.redisson.solon.RedissonClientOriginalSupplier;

/**
 * Redis配置类
 */
@Configuration
public class RedisConfig {

    @Bean(value = "redisClient", typed = true)
    public RedissonClient redisClient(@Inject("${redis.ds1}") RedissonClientOriginalSupplier supplier) {
        return supplier.get();
    }
}
