package com.xiaobai1226.aether.core.task;

import com.xiaobai1226.aether.core.cache.DownloadTaskCache;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.enums.DownloadTaskStatusEnum;
import com.xiaobai1226.aether.core.domain.dto.DownloadTaskDTO;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final long ORPHAN_DOWNLOAD_TEMP_FILE_TIMEOUT_MS = 2L * 60 * 60 * 1000;
    private static final String DOWNLOAD_ZIP_TEMP_DIR = "download";

    @Inject
    private DownloadTaskCache downloadTaskCache;

    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 每10分钟清理一次下载任务和临时文件
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanupDownloadTasks() {
        try {
            downloadTaskCache.cleanupExpiredTasks();
            long now = System.currentTimeMillis();
            var tasks = downloadTaskCache.listTasks();
            for (var task : tasks) {
                if (shouldRemove(task, now)) {
                    downloadTaskCache.removeTask(task.getTaskId());
                }
            }
            cleanupOrphanDownloadTempFiles(tasks, now);
        } catch (Exception e) {
            log.error("下载任务清理失败", e);
        }
    }

    private void cleanupOrphanDownloadTempFiles(List<DownloadTaskDTO> tasks, long now) {
        String tempDirPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, DOWNLOAD_ZIP_TEMP_DIR);
        File tempDir = FileUtil.file(tempDirPath);
        if (!FileUtil.exist(tempDir) || !tempDir.isDirectory()) {
            return;
        }

        Set<String> activeFilePaths = new HashSet<>();
        for (var task : tasks) {
            if (task.getFilePath() != null) {
                activeFilePaths.add(FileUtil.file(task.getFilePath()).getAbsolutePath());
            }
        }

        int deletedCount = 0;
        var tempFiles = FileUtil.loopFiles(tempDir, file -> file.isFile());
        for (var tempFile : tempFiles) {
            try {
                String absolutePath = tempFile.getAbsolutePath();
                if (activeFilePaths.contains(absolutePath)) {
                    continue;
                }

                long lastModified = tempFile.lastModified();
                if (lastModified <= 0) {
                    lastModified = now;
                }
                if (now - lastModified < ORPHAN_DOWNLOAD_TEMP_FILE_TIMEOUT_MS) {
                    continue;
                }

                if (FileUtil.del(tempFile)) {
                    deletedCount++;
                    log.info("删除下载孤立临时文件: {}", absolutePath);
                }
            } catch (Exception e) {
                log.warn("处理下载临时文件失败: {}", tempFile.getAbsolutePath(), e);
            }
        }

        if (deletedCount > 0) {
            log.info("下载临时目录兜底清理完成，共删除 {} 个孤立文件", deletedCount);
        }
    }

    private boolean shouldRemove(DownloadTaskDTO task, long now) {
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
