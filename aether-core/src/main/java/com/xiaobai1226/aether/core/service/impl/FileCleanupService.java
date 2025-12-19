package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.support.FilePurgeService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件清理服务
 * 
 * 职责：查找和清理无引用的文件
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileCleanupService {

    @Db
    private FileMapper fileMapper;

    @Db
    private UserFileMapper userFileMapper;

    @Inject
    private FilePurgeService filePurgeService;

    @Inject
    private FileThumbnailService fileThumbnailService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 查找无引用的文件（使用 SQL 联表查询，高效）
     * 
     * @param limit 查询数量限制
     * @return 无引用的文件ID列表
     */
    public List<Long> findOrphanFiles(int limit) {
        try {
            // 使用 SQL 联表查询，效率高
            List<Long> orphanFileIds = fileMapper.findOrphanFileIds(limit);
            log.info("发现{}个无引用文件", orphanFileIds.size());
            return orphanFileIds;
        } catch (Exception e) {
            log.error("查找无引用文件失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取文件引用计数
     * 
     * @param fileId 文件ID
     * @return 引用计数
     */
    public int getReferenceCount(Long fileId) {
        if (fileId == null) {
            return 0;
        }

        return userFileMapper.selectCount(new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFileDO::getFileId, fileId))
                .intValue();
    }

    /**
     * 仅清理 File 数据库记录，不删除缩略图和物理文件
     * 
     * 说明：
     * - 缩略图由专门的定时任务清理（cleanOrphanThumbnails）
     * - 物理文件由专门的定时任务清理（cleanOrphanPhysicalFiles）
     * 
     * @param fileIds 要清理的文件ID列表
     */
    @Tran
    public void cleanupFileRecordsOnly(List<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        log.info("开始清理 File 数据库记录：共{}个", fileIds.size());

        try {
            // 只删除数据库记录
            int delFileCount = fileMapper.deleteByIds(fileIds);
            if (delFileCount != fileIds.size()) {
                log.warn("删除 FileDO 数量不一致，期望={}, 实际={}", fileIds.size(), delFileCount);
            }

            log.info("清理 File 记录完成：共清理{}个", delFileCount);

        } catch (Exception e) {
            log.error("清理 File 记录失败", e);
            throw e;
        }
    }

    /**
     * 清理存储源中的孤立物理文件
     * 
     * 说明：扫描存储源目录，找出数据库中不存在的物理文件并删除
     */
    public void cleanupOrphanPhysicalFiles() {
        log.info("开始清理孤立物理文件");

        try {
            // 1. 获取所有存储源
            List<StorageSourceDO> storageSources = storageSourceService.getAllStorageSources();
            if (CollUtil.isEmpty(storageSources)) {
                log.info("没有可用的存储源");
                return;
            }

            int totalDeletedCount = 0;

            // 2. 逐个存储源扫描
            for (StorageSourceDO storageSource : storageSources) {
                try {
                    int deletedCount = cleanupOrphanPhysicalFilesInStorage(storageSource);
                    totalDeletedCount += deletedCount;
                } catch (Exception e) {
                    log.error("清理存储源孤立文件失败: storageSourceId={}", storageSource.getId(), e);
                }
            }

            log.info("孤立物理文件清理完成：共删除{}个", totalDeletedCount);

        } catch (Exception e) {
            log.error("清理孤立物理文件任务失败", e);
        }
    }

    /**
     * 清理指定存储源中的孤立物理文件（统一使用 StorageBackend）
     */
    private int cleanupOrphanPhysicalFilesInStorage(StorageSourceDO storageSource) {
        log.info("开始扫描存储源: id={}, path={}", storageSource.getId(), storageSource.getPath());

        int deletedCount = 0;

        try {
            var backend = storageBackendFactory.getByType(storageSource.getType());
            String uploadPath = FileUtils.generatePath(storageSource.getPath(), FolderNameConsts.PATH_UPLOAD_FILE_FULL);

            // 扫描上传目录
            if (!FileUtil.exist(uploadPath)) {
                log.warn("上传目录不存在: {}", uploadPath);
                return 0;
            }

            List<File> physicalFiles = FileUtil.loopFiles(uploadPath);
            log.debug("存储源中共有{}个物理文件", physicalFiles.size());

            for (File physicalFile : physicalFiles) {
                try {
                    // 获取相对路径
                    String relativePath = physicalFile.getAbsolutePath()
                            .substring(storageSource.getPath().length() + 1)
                            .replace("\\", "/");

                    // 检查数据库中是否存在
                    long count = ChainWrappers.lambdaQueryChain(fileMapper)
                            .eq(FileDO::getPath, relativePath)
                            .eq(FileDO::getStorageSourceId, storageSource.getId())
                            .count();

                    if (count == 0) {
                        // 数据库中不存在，使用 StorageBackend 删除物理文件
                        backend.delete(storageSource.getPath(), relativePath);
                        deletedCount++;
                        log.debug("删除孤立物理文件: {}", relativePath);
                    }
                } catch (Exception e) {
                    log.warn("处理物理文件失败: {}", physicalFile.getAbsolutePath(), e);
                }
            }

            log.info("存储源清理完成: storageSourceId={}, 删除{}个孤立文件", 
                    storageSource.getId(), deletedCount);

        } catch (Exception e) {
            log.error("扫描存储源失败: storageSourceId={}", storageSource.getId(), e);
        }

        return deletedCount;
    }

    /**
     * 清理孤立的缩略图
     * 
     * 说明：扫描缩略图目录，找出数据库中没有引用的缩略图并删除
     */
    public void cleanupOrphanThumbnails() {
        log.info("开始清理孤立缩略图");

        try {
            String thumbnailDir = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL);
            
            if (!FileUtil.exist(thumbnailDir)) {
                log.warn("缩略图目录不存在: {}", thumbnailDir);
                return;
            }

            List<File> thumbnailFiles = FileUtil.loopFiles(thumbnailDir);
            log.info("缩略图目录中共有{}个文件", thumbnailFiles.size());

            int deletedCount = 0;

            for (File thumbnailFile : thumbnailFiles) {
                try {
                    // 获取相对路径（相对于缩略图根目录）
                    String relativePath = thumbnailFile.getAbsolutePath()
                            .substring(thumbnailDir.length() + 1)
                            .replace("\\", "/");

                    // 检查数据库中是否有 File 记录引用这个缩略图
                    long refCount = ChainWrappers.lambdaQueryChain(fileMapper)
                            .eq(FileDO::getThumbnail, relativePath)
                            .count();

                    if (refCount == 0) {
                        // 没有引用，删除孤立缩略图
                        FileUtil.del(thumbnailFile);
                        deletedCount++;
                        log.debug("删除孤立缩略图: {}", relativePath);
                    }
                } catch (Exception e) {
                    log.warn("处理缩略图文件失败: {}", thumbnailFile.getAbsolutePath(), e);
                }
            }

            log.info("孤立缩略图清理完成：共删除{}个", deletedCount);

        } catch (Exception e) {
            log.error("清理孤立缩略图任务失败", e);
        }
    }

}