package com.xiaobai1226.aether.core.cache;

import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.xiaobai1226.aether.core.constant.CacheKeyConsts;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;

import static com.xiaobai1226.aether.common.constant.SystemConsts.UPLOADING_USED_STORAGE_TIMEOUT;

/**
 * 用户缓存DAO
 *
 * @author bai
 */
@Component
public class UserCache {

    /**
     * 上传中文件大小缓存
     */
    @Inject("uploadingSizeCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<Long>> uploadingSizeCache;

    /**
     * 增加上传中文件大小
     *
     * @param userId   用户id
     * @param fileSize 文件大小
     */
    public void incrementUploadingFileSize(Long userId, Long fileSize) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.USER, CacheKeyConsts.UPLOADING, CacheKeyConsts.USED_STORAGE, userId);
        
        // 获取当前值
        long currentValue = 0L;
        CaffeineCacheConfig.CacheEntry<Long> entry = uploadingSizeCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            currentValue = entry.getValue();
        }
        
        // 增加值
        long newValue = currentValue + fileSize;
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOADING_USED_STORAGE_TIMEOUT).toMillis();
        CaffeineCacheConfig.CacheEntry<Long> newEntry = new CaffeineCacheConfig.CacheEntry<>(newValue, expireTime);
        uploadingSizeCache.put(key, newEntry);
    }

    /**
     * 减少上传中文件大小
     *
     * @param userId   用户id
     * @param fileSize 文件大小
     */
    public void decrementUploadingFileSize(Long userId, Long fileSize) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.USER, CacheKeyConsts.UPLOADING, CacheKeyConsts.USED_STORAGE, userId);
        
        // 获取当前值
        long currentValue = 0L;
        CaffeineCacheConfig.CacheEntry<Long> entry = uploadingSizeCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            currentValue = entry.getValue();
        }
        
        // 减少值
        long newValue = currentValue - fileSize;
        if (newValue < 0) {
            newValue = 0;
        }
        
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOADING_USED_STORAGE_TIMEOUT).toMillis();
        CaffeineCacheConfig.CacheEntry<Long> newEntry = new CaffeineCacheConfig.CacheEntry<>(newValue, expireTime);
        uploadingSizeCache.put(key, newEntry);
    }

    /**
     * 获取上传中文件大小
     *
     * @param userId 用户id
     * @return 上传中文件大小
     */
    public Long getUploadingFileSize(Long userId) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.USER, CacheKeyConsts.UPLOADING, CacheKeyConsts.USED_STORAGE, userId);
        
        CaffeineCacheConfig.CacheEntry<Long> entry = uploadingSizeCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }
        
        // 如果过期了，删除掉
        if (entry != null) {
            uploadingSizeCache.invalidate(key);
        }
        
        return 0L;
    }
}