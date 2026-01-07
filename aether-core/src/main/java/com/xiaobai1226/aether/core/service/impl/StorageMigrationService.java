package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     * 标记文件/文件夹为待迁移（智能处理，自动区分文件和文件夹）
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

        // 如果是文件夹，递归处理所有子文件
        if (UserFileItemTypeEnum.isFolder(userFile.getItemType())) {
            markFolderForMigrationInternal(userFileId, targetStorageSourceId, userId);
        } else {
            // 如果是文件，直接标记
            markSingleFileForMigration(userFileId, userFile, targetStorageSourceId);
        }
    }

    /**
     * 标记文件夹及其所有子文件为待迁移（内部方法）
     * 递归处理逻辑：
     * 1. 显式指定存储源（type=2）的项：跳过，不处理
     * 2. 继承存储源（type=1）的文件夹：更新存储源，不标记迁移，递归处理子项
     * 3. 继承存储源（type=1）的文件：更新存储源，标记迁移
     * 
     * @param folderId              文件夹ID
     * @param targetStorageSourceId 目标存储源ID
     * @param userId                用户ID
     */
    private void markFolderForMigrationInternal(Long folderId, Long targetStorageSourceId, Long userId) {
        var folder = userFileMapper.selectById(folderId);
        if (folder == null) {
            log.warn("文件夹不存在: folderId={}", folderId);
            return;
        }

        // 1. 先处理文件夹本身（如果是继承类型）
        if (folder.getStorageSourceType() == null || folder.getStorageSourceType() == 1) {
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .eq("id", folderId)
                    .set("storage_source_id", targetStorageSourceId));
            log.info("更新文件夹存储源: folderId={}, targetStorageId={}", folderId, targetStorageSourceId);
        } else if (folder.getStorageSourceType() == 2) {
            // 显式指定的文件夹，跳过不处理
            log.info("文件夹为显式指定存储源，跳过: folderId={}", folderId);
            return;
        }

        // 2. 递归收集需要处理的子文件和子文件夹
        List<UserFileDO> inheritedFolders = new ArrayList<>();
        List<UserFileDO> inheritedFiles = new ArrayList<>();

        collectInheritedItems(folderId, userId, inheritedFolders, inheritedFiles);

        log.info("标记文件夹迁移: folderId={}, 子文件夹数={}, 子文件数={}",
                folderId, inheritedFolders.size(), inheritedFiles.size());

        // 3. 批量更新子文件夹存储源（不标记迁移）
        if (CollUtil.isNotEmpty(inheritedFolders)) {
            List<Long> folderIds = inheritedFolders.stream()
                    .map(UserFileDO::getId)
                    .toList();

            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .in("id", folderIds)
                    .set("storage_source_id", targetStorageSourceId));

            log.info("批量更新子文件夹存储源完成: count={}", folderIds.size());
        }

        // 4. 批量更新子文件存储源并标记迁移
        if (CollUtil.isNotEmpty(inheritedFiles)) {
            List<Long> fileIds = inheritedFiles.stream()
                    .map(UserFileDO::getId)
                    .toList();

            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .in("id", fileIds)
                    .set("storage_source_id", targetStorageSourceId)
                    .set("migration_pending", 1));

            log.info("批量标记子文件迁移完成: count={}", fileIds.size());
        }
    }

    /**
     * 标记单个文件为待迁移（内部方法）
     * 
     * @param userFileId            用户文件ID
     * @param userFile              用户文件实体（可选，避免重复查询）
     * @param targetStorageSourceId 目标存储源ID
     */
    private void markSingleFileForMigration(Long userFileId, UserFileDO userFile, Long targetStorageSourceId) {
        // 只标记继承类型的文件
        if (userFile.getStorageSourceType() != null && userFile.getStorageSourceType() == 1) {
            // 标记为待迁移，并更新目标存储源ID（迁移时需要通过此ID判断目标存储源）
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .eq("id", userFileId)
                    .set("migration_pending", 1)
                    .set("storage_source_id", targetStorageSourceId));

            log.info("文件已标记为待迁移: userFileId={}, targetStorageId={}", userFileId, targetStorageSourceId);
        }
    }

    /**
     * 获取所有待迁移的文件列表，不限制数量（供定时任务调用）
     * 
     * @return 待迁移的文件列表
     */
    public List<UserFileDO> getPendingMigrationFiles() {
        return userFileMapper.selectList(
                new LambdaQueryWrapper<UserFileDO>()
                        .eq(UserFileDO::getMigrationPending, 1));
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
            // clearMigrationFlag(userFileId);
            return;
        }

        // 获取当前文件信息
        if (userFile.getFileId() == null) {
            // 文件夹，清除标记
            // clearMigrationFlag(userFileId);
            return;
        }

        var oldFile = fileMapper.selectById(userFile.getFileId());
        if (oldFile == null) {
            log.warn("迁移失败：文件记录不存在, userFileId={}, fileId={}", userFileId, userFile.getFileId());
            // clearMigrationFlag(userFileId);
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
     * 递归收集继承类型的子文件和文件夹
     * 
     * 逻辑：
     * - 显式指定（storageSourceType=2）：跳过该项及其所有子项
     * - 继承（storageSourceType=1）：
     * - 文件夹：加入 folders 列表，递归处理子项
     * - 文件：加入 files 列表
     * - null或其他类型：按继承处理（兼容旧数据）
     * 
     * @param parentId 父文件夹ID
     * @param userId   用户ID
     * @param folders  收集的文件夹列表（输出参数）
     * @param files    收集的文件列表（输出参数）
     */
    private void collectInheritedItems(Long parentId, Long userId,
            List<UserFileDO> folders, List<UserFileDO> files) {
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
            // 跳过显式指定存储源的项（storageSourceType=2）
            if (child.getStorageSourceType() != null && child.getStorageSourceType() == 2) {
                log.debug("跳过显式指定存储源的项: id={}, name={}", child.getId(), child.getName());
                continue; // 跳过该项及其子项
            }

            // 处理继承类型的项（storageSourceType=1 或 null）
            if (UserFileItemTypeEnum.isFolder(child.getItemType())) {
                // 文件夹：加入列表，递归处理子项
                folders.add(child);
                collectInheritedItems(child.getId(), userId, folders, files);
            } else {
                // 文件：加入列表
                files.add(child);
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