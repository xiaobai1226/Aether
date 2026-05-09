package com.xiaobai1226.aether.core.task;

import cn.hutool.core.io.FileUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.cache.FileCache;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.io.File;

/**
 * 上传临时文件清理调度器
 */
@Component
@Slf4j
public class UploadTempFileScheduler {
    private static final long ORPHAN_WEB_DAV_TEMP_TIMEOUT_MS = 2L * 60 * 60 * 1000;
    private static final String WEB_DAV_TEMP_DIR = "webdav";

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private FileCache fileCache;

    /**
     * 每10分钟清理一次上传临时目录
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanupUploadTempFiles() {
        try {
            cleanupMultipartUploadTempDirs();
            cleanupWebDavTempFiles();
        } catch (Exception e) {
            log.error("上传临时文件清理失败", e);
        }
    }

    private void cleanupMultipartUploadTempDirs() {
        String tempRootPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL);
        File tempRootDir = FileUtil.file(tempRootPath);
        if (!FileUtil.exist(tempRootDir) || !tempRootDir.isDirectory()) {
            return;
        }

        int deletedCount = 0;
        File[] userDirs = tempRootDir.listFiles(File::isDirectory);
        if (userDirs == null) {
            return;
        }

        for (File userDir : userDirs) {
            if (WEB_DAV_TEMP_DIR.equals(userDir.getName()) || "download".equals(userDir.getName())) {
                continue;
            }

            Long userId;
            try {
                userId = Long.parseLong(userDir.getName());
            } catch (Exception e) {
                continue;
            }

            File[] taskDirs = userDir.listFiles(File::isDirectory);
            if (taskDirs == null) {
                continue;
            }

            for (File taskDir : taskDirs) {
                try {
                    String taskId = taskDir.getName();
                    if (fileCache.getUploadTempFileInfo(userId, taskId) != null) {
                        continue;
                    }
                    if (FileUtil.del(taskDir)) {
                        deletedCount++;
                        log.info("删除上传孤立临时目录: {}", taskDir.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("处理上传临时目录失败: {}", taskDir.getAbsolutePath(), e);
                }
            }

            tryDeleteEmptyDir(userDir);
        }

        if (deletedCount > 0) {
            log.info("上传分片临时目录清理完成，共删除 {} 个孤立目录", deletedCount);
        }
    }

    private void cleanupWebDavTempFiles() {
        String webDavTempPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, WEB_DAV_TEMP_DIR);
        File webDavDir = FileUtil.file(webDavTempPath);
        if (!FileUtil.exist(webDavDir) || !webDavDir.isDirectory()) {
            return;
        }

        int deletedCount = 0;
        long now = System.currentTimeMillis();
        File[] userDirs = webDavDir.listFiles(File::isDirectory);
        if (userDirs == null) {
            return;
        }

        for (File userDir : userDirs) {
            var tempFiles = FileUtil.loopFiles(userDir, File::isFile);
            for (File tempFile : tempFiles) {
                try {
                    long lastModified = tempFile.lastModified();
                    if (lastModified <= 0) {
                        lastModified = now;
                    }
                    if (now - lastModified < ORPHAN_WEB_DAV_TEMP_TIMEOUT_MS) {
                        continue;
                    }
                    if (FileUtil.del(tempFile)) {
                        deletedCount++;
                        log.info("删除 WebDAV 孤立临时文件: {}", tempFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("处理 WebDAV 临时文件失败: {}", tempFile.getAbsolutePath(), e);
                }
            }
            tryDeleteEmptyDir(userDir);
        }

        tryDeleteEmptyDir(webDavDir);

        if (deletedCount > 0) {
            log.info("WebDAV 临时文件清理完成，共删除 {} 个文件", deletedCount);
        }
    }

    private void tryDeleteEmptyDir(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null && children.length == 0) {
            FileUtil.del(dir);
        }
    }
}
