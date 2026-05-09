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
import java.util.*;
import java.util.stream.Collectors;

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
     * 原子更新已上传分片信息（幂等）
     *
     * @param userId     用户ID
     * @param taskId     任务ID
     * @param chunkIndex 分片索引
     * @param chunkSize  分片大小
     * @return 更新后的上传任务信息，不存在则返回 null
     */
    public UploadFileTempDTO updateUploadedChunk(Long userId, String taskId, Integer chunkIndex, Long chunkSize) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CacheKeyConsts.FILE, CacheKeyConsts.UPLOAD, CacheKeyConsts.TEMP, userId, taskId);
        final UploadFileTempDTO[] result = new UploadFileTempDTO[1];

        uploadTempFileCache.asMap().compute(key, (k, entry) -> {
            if (entry == null || entry.isExpired()) {
                result[0] = null;
                return null;
            }

            var uploadFileTempDTO = toUploadFileTempDTO(entry.getValue());
            if (uploadFileTempDTO == null) {
                result[0] = null;
                return null;
            }

            var uploadedIndexSet = parseChunkIndexes(uploadFileTempDTO.getReceivedChunkIndexes());
            if (chunkIndex != null && uploadedIndexSet.add(chunkIndex)) {
                long uploadedSize = uploadFileTempDTO.getUploadedSize() == null ? 0L : uploadFileTempDTO.getUploadedSize();
                uploadFileTempDTO.setUploadedSize(uploadedSize + (chunkSize == null ? 0L : chunkSize));
            }
            uploadFileTempDTO.setReceivedChunkIndexes(joinChunkIndexes(uploadedIndexSet));

            long expireTime = System.currentTimeMillis() + Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT).toMillis();
            var newEntry = new CaffeineCacheConfig.CacheEntry<Object>(uploadFileTempDTO, expireTime);
            result[0] = uploadFileTempDTO;
            return newEntry;
        });

        return result[0];
    }

    /**
     * 获取已上传的分片索引列表
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 分片索引列表
     */
    public List<Integer> getUploadedChunkIndexes(Long userId, String taskId) {
        var uploadFileTempDTO = getUploadTempFileInfo(userId, taskId);
        if (uploadFileTempDTO == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(parseChunkIndexes(uploadFileTempDTO.getReceivedChunkIndexes()));
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

    private UploadFileTempDTO toUploadFileTempDTO(Object value) {
        if (value instanceof UploadFileTempDTO dto) {
            return dto;
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            return BeanUtil.toBean(map, UploadFileTempDTO.class);
        }
        return null;
    }

    private Set<Integer> parseChunkIndexes(String chunkIndexes) {
        if (chunkIndexes == null || chunkIndexes.isBlank()) {
            return new TreeSet<>();
        }
        return Arrays.stream(chunkIndexes.split(","))
                .filter(item -> item != null && !item.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String joinChunkIndexes(Set<Integer> chunkIndexes) {
        if (chunkIndexes == null || chunkIndexes.isEmpty()) {
            return "";
        }
        return chunkIndexes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}