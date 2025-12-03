package com.xiaobai1226.aether.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import java.time.Duration;

/**
 * 缓存配置类
 *
 * @author bai
 */
@Configuration
public class CaffeineCacheConfig {

    /**
     * 验证码缓存
     * 最大1000个条目，使用自定义过期策略
     */
    @Bean("captchaCache")
    public Cache<String, CacheEntry<String>> captchaCache() {
        return Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofHours(1)) // 设置一个较长的默认过期时间，实际过期时间由CacheEntry控制
                .build();
    }

    /**
     * 缓存条目包装类，用于存储值和过期时间
     */
    public static class CacheEntry<T> {
        private final T value;
        private final long expireTime;

        public CacheEntry(T value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public T getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}