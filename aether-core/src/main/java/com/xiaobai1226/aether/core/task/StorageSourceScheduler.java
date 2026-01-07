package com.xiaobai1226.aether.core.task;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 存储源调度器
 * 
 * 职责：管理存储源相关的定时任务（如文件迁移等）
 * 
 * @author bai
 */
@Component
@Slf4j
public class StorageSourceScheduler {

    @Inject
    private StorageMigrationService migrationService;

    /**
     * 任务1：处理待迁移文件
     * 每分钟执行一次
     * 
     * 说明：处理所有待迁移的文件
     */
    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void processPendingMigrations() {
        try {
            // 1. 查找待迁移文件（查询全部符合条件的）
            List<UserFileDO> pendingFiles = migrationService.getPendingMigrationFiles();

            if (CollUtil.isEmpty(pendingFiles)) {
                return;
            }

            log.info("开始处理{}个待迁移文件", pendingFiles.size());

            // 2. 逐个迁移
            int successCount = 0;
            int failCount = 0;

            for (UserFileDO userFile : pendingFiles) {
                try {
                    migrationService.migrateFile(userFile.getId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("迁移文件失败，将在下次重试: userFileId={}, error={}",
                            userFile.getId(), e.getMessage());
                    // 失败了不影响其他文件，下次定时任务会重试
                }
            }

            log.info("迁移任务完成: 成功{}个，失败{}个", successCount, failCount);

        } catch (Exception e) {
            log.error("存储源迁移调度任务执行失败", e);
        }
    }
}