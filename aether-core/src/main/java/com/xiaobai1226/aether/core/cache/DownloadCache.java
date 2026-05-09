package com.xiaobai1226.aether.core.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.xiaobai1226.aether.core.constant.CacheKeyConsts;
import com.xiaobai1226.aether.core.domain.dto.DownloadFileDTO;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.common.constant.SystemConsts.DOWNLOAD_FILE_SIGN_TIMEOUT;

/**
 * 下载缓存DAO
 *
 * @author bai
 */
@Component
public class DownloadCache {

    /**
     * 下载签名缓存
     */
    @Inject("downloadSignCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<Object>> downloadSignCache;

    /**
     * 存储下载Sign数据
     *
     * @param downloadFileDTO 下载文件信息
     * @param sign            下载sign
     */
    public void setDownloadSign(DownloadFileDTO downloadFileDTO, String sign) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.DOWNLOAD, sign);
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(DOWNLOAD_FILE_SIGN_TIMEOUT).toMillis();
        CaffeineCacheConfig.CacheEntry<Object> entry = new CaffeineCacheConfig.CacheEntry<>(downloadFileDTO, expireTime);
        downloadSignCache.put(key, entry);
    }

    /**
     * 获取下载sign数据
     *
     * @param sign 下载sign
     * @return 下载sign对应的文件ID
     */
    public DownloadFileDTO getDownloadInfo(String sign) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.DOWNLOAD, sign);
        
        CaffeineCacheConfig.CacheEntry<Object> entry = downloadSignCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            Object value = entry.getValue();
            if (value instanceof DownloadFileDTO) {
                return (DownloadFileDTO) value;
            }
            // 如果是Map类型，转换为DTO
            if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                if (CollUtil.isNotEmpty(map)) {
                    return BeanUtil.toBean(map, DownloadFileDTO.class);
                }
            }
        }
        
        // 如果过期了，删除掉
        if (entry != null) {
            downloadSignCache.invalidate(key);
        }
        
        return null;
    }
}