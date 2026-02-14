package com.xiaobai1226.aether.core.task;

import com.xiaobai1226.aether.core.cache.DownloadTaskCache;
import com.xiaobai1226.aether.core.enums.DownloadTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.io.File;

/**
 * 下载任务清理调度器
 */
@Component
@Slf4j
public class DownloadTaskScheduler {
    private static final long STALE_RUNNING_TIMEOUT_MS = 2L * 60 * 60 * 1000;
    private static final long FAILED_TIMEOUT_MS = 2L * 60 * 60 * 1000;
    private static final long SUCCEEDED_NOT_DOWNLOADED_TIMEOUT_MS = 6L * 60 * 60 * 1000;
    private static final long DOWNLOADED_TIMEOUT_MS = 30L * 60 * 1000;

    @Inject
    private DownloadTaskCache downloadTaskCache;

    /**
     * 每10分钟清理一次下载任务和临时文件
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanupDownloadTasks() {
        try {
            downloadTaskCache.cleanupExpiredTasks();
            long now = System.currentTimeMillis();
            for (var task : downloadTaskCache.listTasks()) {
                if (shouldRemove(task, now)) {
                    downloadTaskCache.removeTask(task.getTaskId());
                }
            }
        } catch (Exception e) {
            log.error("下载任务清理失败", e);
        }
    }

    private boolean shouldRemove(com.xiaobai1226.aether.core.domain.dto.DownloadTaskDTO task, long now) {
        Integer status = task.getStatus();
        long createTime = task.getCreateTime() == null ? now : task.getCreateTime();

        if (status == null) {
            return true;
        }

        if (DownloadTaskStatusEnum.PENDING.flag().equals(status)
                || DownloadTaskStatusEnum.RUNNING.flag().equals(status)) {
            return now - createTime > STALE_RUNNING_TIMEOUT_MS;
        }

        if (DownloadTaskStatusEnum.FAILED.flag().equals(status)) {
            return now - createTime > FAILED_TIMEOUT_MS;
        }

        if (DownloadTaskStatusEnum.SUCCEEDED.flag().equals(status)) {
            if (task.getFilePath() == null || !new File(task.getFilePath()).exists()) {
                return true;
            }
            if (Boolean.TRUE.equals(task.getDownloaded())) {
                long downloadTime = task.getDownloadTime() == null ? now : task.getDownloadTime();
                return now - downloadTime > DOWNLOADED_TIMEOUT_MS;
            }
            return now - createTime > SUCCEEDED_NOT_DOWNLOADED_TIMEOUT_MS;
        }

        return false;
    }
}
