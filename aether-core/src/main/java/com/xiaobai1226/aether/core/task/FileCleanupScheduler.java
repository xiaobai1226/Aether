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
 * 职责：定时清理孤立的文件记录、物理文件和缩略图
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileCleanupScheduler {

    @Inject
    private FileCleanupService cleanupService;

    /**
     * 任务1：清理孤立的 File 数据库记录
     * 每小时执行一次
     * 
     * 说明：仅删除数据库记录，不删除缩略图和物理文件
     * - 缩略图由任务3清理
     * - 物理文件由任务2清理
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanOrphanFileRecords() {
        try {
            log.info("开始清理孤立 File 记录");

            // 1. 查找无引用的文件（每次最多10000个）
            List<Long> orphanFileIds = cleanupService.findOrphanFiles(10000);

            if (CollUtil.isEmpty(orphanFileIds)) {
                log.info("没有需要清理的孤立 File 记录");
                return;
            }

            log.info("发现 {} 个孤立 File 记录，开始清理", orphanFileIds.size());

            // 2. 仅清理数据库记录
            cleanupService.cleanupFileRecordsOnly(orphanFileIds);

            log.info("孤立 File 记录清理完成，共清理 {} 个", orphanFileIds.size());

        } catch (Exception e) {
            log.error("清理孤立 File 记录任务执行失败", e);
        }
    }

    /**
     * 任务2：清理存储源中的孤立物理文件
     * 每天凌晨 3 点执行
     * 
     * 说明：扫描存储源目录，找出数据库中不存在的物理文件并删除
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOrphanPhysicalFiles() {
        try {
            log.info("开始清理孤立物理文件任务");
            cleanupService.cleanupOrphanPhysicalFiles();
            log.info("孤立物理文件清理任务完成");
        } catch (Exception e) {
            log.error("清理孤立物理文件任务执行失败", e);
        }
    }

    /**
     * 任务3：清理孤立的缩略图
     * 每周日凌晨 4 点执行
     * 
     * 说明：扫描缩略图目录，找出数据库中没有引用的缩略图并删除
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    public void cleanOrphanThumbnails() {
        try {
            log.info("开始清理孤立缩略图任务");
            cleanupService.cleanupOrphanThumbnails();
            log.info("孤立缩略图清理任务完成");
        } catch (Exception e) {
            log.error("清理孤立缩略图任务执行失败", e);
        }
    }
}