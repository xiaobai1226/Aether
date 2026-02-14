package com.xiaobai1226.aether.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import java.time.Duration;

import static com.xiaobai1226.aether.common.constant.SystemConsts.DOWNLOAD_FILE_SIGN_TIMEOUT;

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
     * 上传临时文件信息缓存
     * 最大10000个条目，60分钟后过期
     */
    @Bean("uploadTempFileCache")
    public Cache<String, CacheEntry<Object>> uploadTempFileCache() {
        return Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(Duration.ofMinutes(60))
                .build();
    }

    /**
     * 上传中文件大小缓存
     * 最大10000个条目，60分钟后过期
     */
    @Bean("uploadingSizeCache")
    public Cache<String, CacheEntry<Long>> uploadingSizeCache() {
        return Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(Duration.ofMinutes(60))
                .build();
    }

    /**
     * 分享文件信息缓存
     * 最大5000个条目，60分钟后过期
     */
    @Bean("shareInfoCache")
    public Cache<String, CacheEntry<Object>> shareInfoCache() {
        return Caffeine.newBuilder().maximumSize(5000).expireAfterWrite(Duration.ofMinutes(60))
                .build();
    }

    /**
     * 下载签名缓存
     * 最大5000个条目，按配置时间后过期
     */
    @Bean("downloadSignCache")
    public Cache<String, CacheEntry<Object>> downloadSignCache() {
        return Caffeine.newBuilder().maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(DOWNLOAD_FILE_SIGN_TIMEOUT))
                .build();
    }

    /**
     * 下载任务缓存
     * 最大2000个条目，6小时后过期
     */
    @Bean("downloadTaskCache")
    public Cache<String, CacheEntry<Object>> downloadTaskCache() {
        return Caffeine.newBuilder().maximumSize(2000).expireAfterWrite(Duration.ofHours(6))
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