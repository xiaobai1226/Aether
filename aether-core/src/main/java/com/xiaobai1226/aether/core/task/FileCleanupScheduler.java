package com.xiaobai1226.aether.core.task;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.service.impl.FileCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 文件清理调度器
 * 
 * 职责：定时清理无引用的文件（物理文件和数据库记录）
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileCleanupScheduler {

    @Inject
    private FileCleanupService cleanupService;

    /**
     * 每小时执行一次清理任务
     * 
     * 说明：查找无引用的File记录并删除物理文件和数据库记录
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanOrphanFiles() {
        try {
            log.info("开始扫描无引用文件");

            // 1. 查找无引用的文件
            List<Long> orphanFileIds = cleanupService.findOrphanFiles();

            if (CollUtil.isEmpty(orphanFileIds)) {
                log.info("没有需要清理的无引用文件");
                return;
            }

            log.info("发现{}个无引用文件，开始清理", orphanFileIds.size());

            // 2. 批量清理
            cleanupService.cleanupFiles(orphanFileIds);

            log.info("文件清理完成，共清理{}个文件", orphanFileIds.size());

        } catch (Exception e) {
            log.error("文件清理任务执行失败", e);
        }
    }
}