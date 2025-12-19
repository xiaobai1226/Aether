package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.intf.FileService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_NO_STORAGE_SOURCE;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;

/**
 * 存储源迁移服务
 * 
 * 职责：处理文件在不同存储源之间的迁移
 * 
 * @author bai
 */
@Component
@Slf4j
public class StorageMigrationService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private FileMapper fileMapper;

    @Inject
    private FileService fileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    /**
     * 标记文件/文件夹为待迁移（异步迁移入口）
     * 
     * @param userFileId            用户文件ID
     * @param targetStorageSourceId 目标存储源ID
     * @param userId                用户ID
     */
    public void markForMigration(Long userFileId, Long targetStorageSourceId, Long userId) {
        var userFile = userFileMapper.selectById(userFileId);
        if (userFile == null || !Objects.equals(userFile.getUserId(), userId)) {
            log.warn("标记迁移失败：文件不存在或无权限, userFileId={}", userFileId);
            return;
        }

        // 只标记继承类型的文件
        if (userFile.getStorageSourceType() != null && userFile.getStorageSourceType() == 1) {
            // 标记为待迁移
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .eq("id", userFileId)
                    .set("migration_pending", 1));

            log.info("文件已标记为待迁移: userFileId={}, targetStorageId={}", userFileId, targetStorageSourceId);
        }
    }

    /**
     * 标记文件夹及其所有子文件为待迁移
     * 
     * @param folderId              文件夹ID
     * @param targetStorageSourceId 目标存储源ID
     * @param userId                用户ID
     */
    public void markFolderForMigration(Long folderId, Long targetStorageSourceId, Long userId) {
        // 递归查找所有子文件
        List<UserFileDO> allFiles = new ArrayList<>();
        collectSubFiles(folderId, userId, allFiles);

        log.info("标记文件夹迁移: folderId={}, 共{}个文件", folderId, allFiles.size());

        // 批量标记
        for (UserFileDO file : allFiles) {
            if (UserFileItemTypeEnum.isFile(file.getItemType())
                    && file.getStorageSourceType() != null
                    && file.getStorageSourceType() == 1) {
                markForMigration(file.getId(), targetStorageSourceId, userId);
            }
        }
    }

    /**
     * 迁移单个文件（同步执行，由定时任务调用）
     * 
     * @param userFileId 用户文件ID
     */
    @Tran
    public void migrateFile(Long userFileId) {
        var userFile = userFileMapper.selectById(userFileId);
        if (userFile == null) {
            log.warn("迁移失败：文件不存在, userFileId={}", userFileId);
            return;
        }

        // 检查是否还需要迁移
        if (userFile.getMigrationPending() == null || userFile.getMigrationPending() == 0) {
            log.info("文件已被其他进程处理，无需迁移: userFileId={}", userFileId);
            return;
        }

        // 获取目标存储源ID
        Long targetStorageSourceId = userFile.getStorageSourceId();
        if (targetStorageSourceId == null) {
            log.warn("迁移失败：目标存储源为空, userFileId={}", userFileId);
            clearMigrationFlag(userFileId);
            return;
        }

        // 获取当前文件信息
        if (userFile.getFileId() == null) {
            // 文件夹，清除标记
            clearMigrationFlag(userFileId);
            return;
        }

        var oldFile = fileMapper.selectById(userFile.getFileId());
        if (oldFile == null) {
            log.warn("迁移失败：文件记录不存在, userFileId={}, fileId={}", userFileId, userFile.getFileId());
            clearMigrationFlag(userFileId);
            return;
        }

        Long sourceStorageSourceId = oldFile.getStorageSourceId();

        // 检查是否需要迁移
        if (Objects.equals(sourceStorageSourceId, targetStorageSourceId)) {
            log.info("文件已在目标存储源，无需迁移: userFileId={}", userFileId);
            clearMigrationFlag(userFileId);
            return;
        }

        // 执行迁移
        try {
            var sourceStorage = storageSourceService.getStorageSourceById(sourceStorageSourceId, userFile.getUserId());
            var targetStorage = storageSourceService.getStorageSourceById(targetStorageSourceId, userFile.getUserId());

            if (sourceStorage == null || targetStorage == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            // 复制文件到新存储源
            var newFile = copyFileToStorage(oldFile, sourceStorage, targetStorage);
            if (newFile == null) {
                throw new FailResultException(SYSTEM_ERROR, "复制文件失败");
            }

            // 更新UserFile的fileId指向新文件
            userFile.setFileId(newFile.getId());
            userFile.setMigrationPending(0);
            userFileMapper.updateById(userFile);

            log.info("文件迁移成功: userFileId={}, oldFileId={}, newFileId={}",
                    userFileId, oldFile.getId(), newFile.getId());
        } catch (Exception e) {
            log.error("文件迁移失败: userFileId={}", userFileId, e);
            throw new FailResultException(SYSTEM_ERROR, "文件迁移失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否需要迁移
     * 
     * @param userFile 用户文件
     * @return 是否需要迁移
     */
    public boolean needsMigration(UserFileDO userFile) {
        if (userFile == null || userFile.getFileId() == null) {
            return false;
        }

        // 只有继承类型的文件才可能需要迁移
        if (userFile.getStorageSourceType() == null || userFile.getStorageSourceType() != 1) {
            return false;
        }

        var file = fileMapper.selectById(userFile.getFileId());
        if (file == null) {
            return false;
        }

        // 比较存储源是否一致
        return !Objects.equals(file.getStorageSourceId(), userFile.getStorageSourceId());
    }

    /**
     * 复制文件到新存储源
     * 
     * @param sourceFile    源文件DO
     * @param sourceStorage 源存储源
     * @param targetStorage 目标存储源
     * @return 新文件DO
     */
    public FileDO copyFileToStorage(FileDO sourceFile, StorageSourceDO sourceStorage, StorageSourceDO targetStorage) {
        return fileService.copyFileToStorageSource(
                sourceFile,
                sourceStorage.getPath(),
                targetStorage.getPath(),
                targetStorage.getId());
    }

    /**
     * 递归收集子文件
     */
    private void collectSubFiles(Long parentId, Long userId, List<UserFileDO> result) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var children = lambdaQuery
                .eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getParentId, parentId)
                .eq(UserFileDO::getFileStatus, 1)
                .list();

        if (CollUtil.isEmpty(children)) {
            return;
        }

        for (var child : children) {
            result.add(child);
            if (UserFileItemTypeEnum.isFolder(child.getItemType())) {
                collectSubFiles(child.getId(), userId, result);
            }
        }
    }

    /**
     * 清除迁移标记
     */
    private void clearMigrationFlag(Long userFileId) {
        userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                .eq("id", userFileId)
                .set("migration_pending", 0));
    }
}