package com.xiaobai1226.aether.core.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 存储源迁移调度器
 * 
 * 职责：定时扫描待迁移文件并执行迁移
 * 
 * @author bai
 */
@Component
@Slf4j
public class StorageMigrationScheduler {

    @Db
    private UserFileMapper userFileMapper;

    @Inject
    private StorageMigrationService migrationService;

    /**
     * 每分钟执行一次迁移任务
     * 
     * 说明：每次处理10个文件，避免占用过多资源
     */
    @Scheduled(fixedDelay = 60000)
    public void processPendingMigrations() {
        try {
            // 1. 查找待迁移文件（每次处理10个）
            List<UserFileDO> pendingFiles = userFileMapper.selectList(
                    new LambdaQueryWrapper<UserFileDO>()
                            .eq(UserFileDO::getMigrationPending, 1)
                            .last("LIMIT 10")
            );

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
            log.error("迁移调度任务执行失败", e);
        }
    }

    /**
     * 获取待迁移文件数量（用于监控）
     */
    public long getPendingMigrationCount() {
        return userFileMapper.selectCount(
                new LambdaQueryWrapper<UserFileDO>()
                        .eq(UserFileDO::getMigrationPending, 1)
        );
    }
}