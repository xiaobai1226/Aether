package com.xiaobai1226.aether.core.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.xiaobai1226.aether.core.constant.CacheKeyConsts;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import com.xiaobai1226.aether.dao.domain.dto.ShareFileDTO;
import com.github.benmanes.caffeine.cache.Cache;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.common.constant.SystemConsts.UPLOAD_TEMP_FILE_INFO_TIMEOUT;

/**
 * 分享缓存DAO
 *
 * @author bai
 */
@Component
public class ShareCache {

    /**
     * 分享文件信息缓存
     */
    @Inject("shareInfoCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<Object>> shareInfoCache;

    /**
     * 存储分享文件信息
     *
     * @param shareId      分享id
     * @param shareFileDTO 分享文件信息
     */
    public void putShareInfo(String shareId, ShareFileDTO shareFileDTO) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.SHARE, shareId);
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT).toMillis();
        CaffeineCacheConfig.CacheEntry<Object> entry = new CaffeineCacheConfig.CacheEntry<>(shareFileDTO, expireTime);
        shareInfoCache.put(key, entry);
    }

    /**
     * 获取分享文件信息
     *
     * @param shareId 分享id
     * @return 分享文件信息
     */
    public ShareFileDTO getShareInfo(String shareId) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.SHARE, shareId);
        
        CaffeineCacheConfig.CacheEntry<Object> entry = shareInfoCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            Object value = entry.getValue();
            if (value instanceof ShareFileDTO) {
                return (ShareFileDTO) value;
            }
            // 如果是Map类型，转换为DTO
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                if (CollUtil.isNotEmpty(map)) {
                    return BeanUtil.toBean(map, ShareFileDTO.class);
                }
            }
        }
        
        // 如果过期了，删除掉
        if (entry != null) {
            shareInfoCache.invalidate(key);
        }
        
        return null;
    }
}