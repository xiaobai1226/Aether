package com.xiaobai1226.aether.core.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.impl.FileCleanupService;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.support.ThumbnailService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缩略图调度器
 * 
 * 职责：管理缩略图的生成和清理
 * 
 * @author bai
 */
@Component
@Slf4j
public class ThumbnailScheduler {

    @Db
    private FileMapper fileMapper;

    @Inject
    private FileService fileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject
    private FileCleanupService cleanupService;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private ThumbnailService thumbnailService;

    /**
     * 任务1：定时生成缺失的缩略图
     * 每 2 分钟执行一次
     * 
     * 说明：补偿生成那些应该有缩略图但还没生成的文件
     */
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void generateMissingThumbnails() {
        try {
            // 1. 查找需要生成缩略图但还没有的文件
            // file_type: 1=视频, 3=图片
            // 排除刚创建的记录（1分钟内），给上传流程留出时间
            List<FileDO> missingThumbnailFiles = fileMapper.selectList(
                    new LambdaQueryWrapper<FileDO>()
                            .isNull(FileDO::getThumbnail)
                            .in(FileDO::getFileType, 1, 3) // 视频和图片
                            .lt(FileDO::getCreateTime, LocalDateTime.now().minusMinutes(1)) // 排除刚创建的
                            .orderByAsc(FileDO::getCreateTime) // 按创建时间升序
            );

            if (CollUtil.isEmpty(missingThumbnailFiles)) {
                return;
            }

            log.info("开始补偿生成缩略图，共 {} 个文件", missingThumbnailFiles.size());

            int successCount = 0;
            int failCount = 0;

            // 2. 逐个生成缩略图
            for (FileDO fileDO : missingThumbnailFiles) {
                try {
                    if (generateThumbnailForFile(fileDO)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("生成缩略图异常: fileId={}", fileDO.getId(), e);
                }
            }

            log.info("缩略图补偿生成完成：成功 {} 个，失败 {} 个", successCount, failCount);

        } catch (Exception e) {
            log.error("缩略图生成任务执行失败", e);
        }
    }

    /**
     * 任务2：清理孤立的缩略图
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

    /**
     * 为单个文件生成缩略图
     * 
     * @param fileDO 文件DO
     * @return 是否成功
     */
    private boolean generateThumbnailForFile(FileDO fileDO) {
        try {
            // 1. 获取文件物理路径
            var storageSource = storageSourceService.getStorageSourceById(fileDO.getStorageSourceId());
            if (storageSource == null) {
                log.warn("存储源不存在，跳过: fileId={}, storageSourceId={}",
                        fileDO.getId(), fileDO.getStorageSourceId());
                return false;
            }

            var backend = storageBackendFactory.getByType(storageSource.getType());
            String absolutePath = backend.tryResolveAbsolutePath(
                    storageSource.getPath(),
                    fileDO.getPath());

            if (StrUtil.isBlank(absolutePath) || !FileUtil.exist(absolutePath)) {
                log.warn("文件不存在，跳过: fileId={}, path={}", fileDO.getId(), fileDO.getPath());
                return false;
            }

            // 2. 生成缩略图（直接使用 ThumbnailService）
            var thumbnailResult = thumbnailService.generateThumbnail(
                    absolutePath,
                    fileDO.getName(),
                    fileDO.getSize());

            if (!thumbnailResult.isSuccess()) {
                log.warn("缩略图生成失败: fileId={}, path={}", fileDO.getId(), fileDO.getPath());
                return false;
            }

            // 3. 更新数据库
            fileService.updateThumbnail(fileDO.getId(), thumbnailResult.getThumbnailFileName());
            log.info("缩略图补偿生成成功: fileId={}, thumbnail={}", fileDO.getId(), 
                    thumbnailResult.getThumbnailFileName());
            return true;

        } catch (Exception e) {
            log.error("为文件生成缩略图失败: fileId={}", fileDO.getId(), e);
            return false;
        }
    }
}