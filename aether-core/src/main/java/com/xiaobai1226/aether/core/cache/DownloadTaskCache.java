package com.xiaobai1226.aether.core.cache;

import cn.hutool.core.io.FileUtil;
import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.xiaobai1226.aether.core.domain.dto.DownloadTaskDTO;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.DOWNLOAD;
import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.FILE;
import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.SIGN;
import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.TASK;

/**
 * 下载任务缓存DAO
 */
@Component
public class DownloadTaskCache {
    private static final Duration DOWNLOAD_TASK_FILE_EXPIRE = Duration.ofHours(2);

    @Inject("downloadTaskCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<Object>> downloadTaskCache;

    public void setTask(DownloadTaskDTO taskDTO) {
        String key = buildKey(taskDTO.getTaskId());
        long expireTime = System.currentTimeMillis() + DOWNLOAD_TASK_FILE_EXPIRE.toMillis();
        taskDTO.setExpireTime(expireTime);
        downloadTaskCache.put(key, new CaffeineCacheConfig.CacheEntry<>(taskDTO, expireTime));
        if (taskDTO.getDownloadSign() != null) {
            downloadTaskCache.put(buildSignKey(taskDTO.getDownloadSign()),
                    new CaffeineCacheConfig.CacheEntry<>(taskDTO.getTaskId(), expireTime));
        }
    }

    public DownloadTaskDTO getTask(String taskId) {
        String key = buildKey(taskId);
        CaffeineCacheConfig.CacheEntry<Object> entry = downloadTaskCache.getIfPresent(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            clearTaskFile(entry.getValue());
            downloadTaskCache.invalidate(key);
            return null;
        }

        Object value = entry.getValue();
        if (value instanceof DownloadTaskDTO taskDTO) {
            return taskDTO;
        }
        return null;
    }

    public void removeTask(String taskId) {
        String key = buildKey(taskId);
        CaffeineCacheConfig.CacheEntry<Object> entry = downloadTaskCache.getIfPresent(key);
        if (entry != null) {
            clearTaskSign(entry.getValue());
            clearTaskFile(entry.getValue());
        }
        downloadTaskCache.invalidate(key);
    }

    public String getTaskIdBySign(String sign) {
        String key = buildSignKey(sign);
        CaffeineCacheConfig.CacheEntry<Object> entry = downloadTaskCache.getIfPresent(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                downloadTaskCache.invalidate(key);
            }
            return null;
        }
        Object value = entry.getValue();
        if (value instanceof String taskId) {
            return taskId;
        }
        return null;
    }

    public List<DownloadTaskDTO> listTasks() {
        List<DownloadTaskDTO> tasks = new ArrayList<>();
        ConcurrentMap<String, CaffeineCacheConfig.CacheEntry<Object>> map = downloadTaskCache.asMap();
        for (var item : map.entrySet()) {
            var entry = item.getValue();
            if (entry == null || entry.isExpired()) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof DownloadTaskDTO taskDTO) {
                tasks.add(taskDTO);
            }
        }
        return tasks;
    }

    public void markTaskDownloaded(String taskId) {
        DownloadTaskDTO taskDTO = getTask(taskId);
        if (taskDTO == null) {
            return;
        }
        taskDTO.setDownloaded(true);
        taskDTO.setDownloadTime(System.currentTimeMillis());
        setTask(taskDTO);
    }

    public void cleanupExpiredTasks() {
        ConcurrentMap<String, CaffeineCacheConfig.CacheEntry<Object>> map = downloadTaskCache.asMap();
        long now = System.currentTimeMillis();
        for (var item : map.entrySet()) {
            CaffeineCacheConfig.CacheEntry<Object> entry = item.getValue();
            if (entry == null) {
                continue;
            }
            if (entry.getExpireTime() <= now) {
                clearTaskSign(entry.getValue());
                clearTaskFile(entry.getValue());
                downloadTaskCache.invalidate(item.getKey());
            }
        }
    }

    private String buildKey(String taskId) {
        return CacheKeyGenerator.PROJECT.generateKey(FILE, DOWNLOAD, TASK, taskId);
    }

    private String buildSignKey(String sign) {
        return CacheKeyGenerator.PROJECT.generateKey(FILE, DOWNLOAD, TASK, SIGN, sign);
    }

    private void clearTaskFile(Object value) {
        if (!(value instanceof DownloadTaskDTO taskDTO)) {
            return;
        }
        String filePath = taskDTO.getFilePath();
        if (filePath != null) {
            FileUtil.del(filePath);
        }
    }

    private void clearTaskSign(Object value) {
        if (!(value instanceof DownloadTaskDTO taskDTO)) {
            return;
        }
        if (taskDTO.getDownloadSign() != null) {
            downloadTaskCache.invalidate(buildSignKey(taskDTO.getDownloadSign()));
        }
    }
}
