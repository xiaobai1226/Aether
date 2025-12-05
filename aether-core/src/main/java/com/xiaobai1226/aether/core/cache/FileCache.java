package com.xiaobai1226.aether.core.cache;

import cn.hutool.core.bean.BeanUtil;
import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.xiaobai1226.aether.core.constant.CacheKeyConsts;
import com.xiaobai1226.aether.core.domain.dto.UploadFileTempDTO;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.UPLOADED_SIZE;
import static com.xiaobai1226.aether.common.constant.SystemConsts.UPLOAD_TEMP_FILE_INFO_TIMEOUT;

/**
 * 文件缓存DAO
 *
 * @author bai
 */
@Component
public class FileCache {

    /**
     * 上传临时文件缓存
     */
    @Inject("uploadTempFileCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<Object>> uploadTempFileCache;

    /**
     * 存储上传文件临时信息
     *
     * @param userId            用户id
     * @param taskId            任务id
     * @param uploadFileTempDTO 上传文件信息
     */
    public void putUploadTempFileInfo(Long userId, String taskId, UploadFileTempDTO uploadFileTempDTO) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.UPLOAD, CacheKeyConsts.TEMP, userId, taskId);
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT).toMillis();
        CaffeineCacheConfig.CacheEntry<Object> entry = new CaffeineCacheConfig.CacheEntry<>(uploadFileTempDTO, expireTime);
        uploadTempFileCache.put(key, entry);
    }

    /**
     * 更新已上传文件大小
     *
     * @param userId    用户id
     * @param taskId    任务id
     * @param chunkSize 上传文件信息
     */
    public void updateUploadedSize(Long userId, String taskId, Long chunkSize) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.UPLOAD, CacheKeyConsts.TEMP, userId, taskId);
        
        // 获取现有的条目
        CaffeineCacheConfig.CacheEntry<Object> entry = uploadTempFileCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            // 转换为Map
            Map<String, Object> uploadFileTempMap = BeanUtil.beanToMap(entry.getValue());
            
            var uploadedSize = 0L;
            var uploadedSizeObject = uploadFileTempMap.get(UPLOADED_SIZE);
            if (uploadedSizeObject != null) {
                uploadedSize = Long.parseLong(String.valueOf(uploadedSizeObject)) + chunkSize;
            } else {
                uploadedSize = chunkSize;
            }
            
            // 更新uploadedSize
            uploadFileTempMap.put(UPLOADED_SIZE, uploadedSize);
            
            // 重新保存
            long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT).toMillis();
            CaffeineCacheConfig.CacheEntry<Object> newEntry = new CaffeineCacheConfig.CacheEntry<>(BeanUtil.toBean(uploadFileTempMap, UploadFileTempDTO.class), expireTime);
            uploadTempFileCache.put(key, newEntry);
        }
    }

    /**
     * 获取上传文件临时信息
     *
     * @param userId 用户id
     * @param taskId 任务id
     * @return id对应的图形验证码数据
     */
    public UploadFileTempDTO getUploadTempFileInfo(Long userId, String taskId) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.UPLOAD, CacheKeyConsts.TEMP, userId, taskId);
        CaffeineCacheConfig.CacheEntry<Object> entry = uploadTempFileCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            Object value = entry.getValue();
            if (value instanceof UploadFileTempDTO) {
                return (UploadFileTempDTO) value;
            }
            // 如果是Map类型，转换为DTO
            if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                return BeanUtil.toBean(map, UploadFileTempDTO.class);
            }
        }
        // 如果过期了，删除掉
        if (entry != null) {
            uploadTempFileCache.invalidate(key);
        }
        return null;
    }

    /**
     * 删除上传文件临时信息
     *
     * @param userId 用户id
     * @param taskId 任务id
     */
    public Boolean delUploadTempFileInfo(Long userId, String taskId) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.UPLOAD, CacheKeyConsts.TEMP, userId, taskId);
        uploadTempFileCache.invalidate(key);
        return true;
    }
}