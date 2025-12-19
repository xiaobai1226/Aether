package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.impl.FileThumbnailService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.Set;

/**
 * 彻底删除文件（物理对象 + FileDO 记录）服务
 *
 * 说明：这是"轻量一致性套路"的一部分：数据库引用先断开/删除，物理对象删除失败不影响接口结果，后续可通过定时任务或人工清理。
 * 
 * @author bai
 */
@Component
@Slf4j
public class FilePurgeService {

    @Db
    private FileMapper fileMapper;

    @Db
    private UserFileMapper userFileMapper;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject
    private FileThumbnailService fileThumbnailService;

    /**
     * 彻底删除文件（用户级操作）
     * 
     * 重要：此方法会检查文件引用计数，只删除没有被其他 UserFile 引用的文件
     * 适用场景：用户主动彻底删除回收站文件
     * 
     * @param userId  用户ID
     * @param fileIds 文件ID集合
     */
    public void purgeFiles(Long userId, Set<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        log.info("用户级彻底删除文件: userId={}, fileIds数量={}", userId, fileIds.size());

        List<FileDO> delFileList = ChainWrappers.lambdaQueryChain(fileMapper)
                .in(FileDO::getId, fileIds)
                .list();
        if (CollUtil.isEmpty(delFileList)) {
            log.warn("要删除的文件不存在: fileIds={}", fileIds);
            return;
        }

        // 检查每个文件的引用计数，只删除没有引用的文件
        int deletedCount = 0;
        int skippedCount = 0;

        for (var delFile : delFileList) {
            // 检查是否还有其他 UserFile 引用这个 File
            long refCount = ChainWrappers.lambdaQueryChain(userFileMapper)
                    .eq(UserFileDO::getFileId, delFile.getId())
                    .count();

            if (refCount > 0) {
                // 还有其他引用，跳过删除（秒传场景）
                log.info("文件仍被引用，跳过删除: fileId={}, refCount={}", delFile.getId(), refCount);
                skippedCount++;
                continue;
            }

            // 没有引用，可以安全删除
            // 1) 删除 FileDO 记录
            fileMapper.deleteById(delFile.getId());

            // 2) 删除缩略图
            deleteThumbnailIfNeeded(delFile);

            // 3) 删除物理文件
            deletePhysicalFile(delFile, userId, false);

            deletedCount++;
            log.debug("文件已彻底删除: fileId={}, path={}", delFile.getId(), delFile.getPath());
        }

        log.info("用户级彻底删除完成: 删除{}个文件, 跳过{}个文件（仍被引用）", deletedCount, skippedCount);
    }

    /**
     * 彻底删除文件（系统级操作，用于定时清理任务）
     * 
     * 注意：系统级操作不需要 userId，直接通过 storageSourceId 查询存储源
     * 
     * @param fileIds 文件ID集合
     */
    public void purgeFilesSystem(Set<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        log.info("系统级清理文件: 共{}个", fileIds.size());

        List<FileDO> delFileList = ChainWrappers.lambdaQueryChain(fileMapper)
                .in(FileDO::getId, fileIds)
                .list();
        if (CollUtil.isEmpty(delFileList)) {
            log.warn("要清理的文件不存在: fileIds={}", fileIds);
            return;
        }

        int delFileCount = fileMapper.deleteByIds(fileIds);
        if (delFileCount != fileIds.size()) {
            log.warn("彻底删除 FileDO 数量不一致，期望={}, 实际={}", fileIds.size(), delFileCount);
        }

        for (var delFile : delFileList) {
            // 1) 删除缩略图
            deleteThumbnailIfNeeded(delFile);

            // 2) 删除物理文件（系统级）
            deletePhysicalFile(delFile, null, true);
        }

        log.info("系统级清理完成: 共清理{}个文件", delFileList.size());
    }

    /**
     * 删除缩略图（如果有）
     * 
     * @param fileDO 文件DO
     */
    private void deleteThumbnailIfNeeded(FileDO fileDO) {
        if (fileDO.getThumbnail() == null) {
            return;
        }

        try {
            // 统一使用 FileThumbnailService 删除缩略图
            fileThumbnailService.deleteThumbnail(fileDO.getThumbnail());
            log.debug("缩略图已删除: fileId={}, thumbnail={}", fileDO.getId(), fileDO.getThumbnail());
        } catch (Exception e) {
            log.warn("删除缩略图失败: fileId={}, thumbnail={}", fileDO.getId(), fileDO.getThumbnail(), e);
        }
    }

    /**
     * 删除物理文件
     * 
     * @param fileDO        文件DO
     * @param userId        用户ID（系统级操作传null）
     * @param isSystemLevel 是否系统级操作
     */
    private void deletePhysicalFile(FileDO fileDO, Long userId, boolean isSystemLevel) {
        try {
            var storageSourceId = fileDO.getStorageSourceId();
            if (storageSourceId == null) {
                log.warn("文件没有存储源ID，跳过物理删除: fileId={}, path={}", fileDO.getId(), fileDO.getPath());
                return;
            }

            // 根据操作级别选择不同的查询方法
            var storageSource = isSystemLevel
                    ? storageSourceService.getStorageSourceById(storageSourceId)
                    : storageSourceService.getStorageSourceById(storageSourceId, userId);

            if (storageSource == null) {
                log.warn("无法获取存储源，跳过物理删除: fileId={}, storageSourceId={}, path={}",
                        fileDO.getId(), storageSourceId, fileDO.getPath());
                return;
            }

            var backend = storageBackendFactory.getByType(storageSource.getType());
            backend.delete(storageSource.getPath(), fileDO.getPath());

            log.debug("物理文件已删除: fileId={}, path={}", fileDO.getId(), fileDO.getPath());
        } catch (Exception e) {
            log.warn("删除物理文件失败（可后续清理）: fileId={}, path={}", fileDO.getId(), fileDO.getPath(), e);
        }
    }
}