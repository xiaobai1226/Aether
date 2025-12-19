package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件移动用例
 * 
 * 职责：编排文件/文件夹移动的完整业务流程
 * 
 * @author bai
 */
@Component
@Slf4j
public class MoveFileUseCase {

    @Inject
    private UserFileService userFileService;

    @Inject
    private StorageMigrationService migrationService;

    @Inject
    private StorageSourceService storageSourceService;

    /**
     * 执行移动操作
     * 
     * @param sourceIds 源文件/文件夹ID列表
     * @param targetId  目标文件夹ID（0表示根目录）
     * @param userId    用户ID
     */
    @Tran
    public void execute(List<Long> sourceIds, Long targetId, Long userId) {
        log.info("开始移动文件: sourceIds={}, targetId={}, userId={}", sourceIds, targetId, userId);

        // 1. 校验源文件
        List<UserFileDO> sourceFiles = userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL);
        validateSourceFiles(sourceFiles, sourceIds);

        // 2. 校验目标文件夹
        validateTargetFolder(targetId, userId);

        // 3. 检查是否移动到当前目录
        if (Objects.equals(sourceFiles.get(0).getParentId(), targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_IN_CURRENT_FOLDER);
        }

        // 4. 检查是否移动到自身或子目录
        validateNotMovingToSubfolder(sourceFiles, targetId, userId);

        // 5. 检查重名
        validateNoNameConflict(sourceFiles, targetId, userId);

        // 6. 更新父目录
        userFileService.updateParentIdByIds(sourceIds, targetId, userId, NORMAL);

        // 7. 处理存储源迁移（异步）
        handleStorageMigrationIfNeeded(sourceFiles, targetId, userId);

        log.info("文件移动完成: sourceIds={}, targetId={}", sourceIds, targetId);
    }

    /**
     * 校验源文件
     */
    private void validateSourceFiles(List<UserFileDO> sourceFiles, List<Long> sourceIds) {
        if (CollUtil.isEmpty(sourceFiles) || sourceFiles.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_CONTENT_EMPTY);
        }
    }

    /**
     * 校验目标文件夹
     */
    private void validateTargetFolder(Long targetId, Long userId) {
        if (targetId != 0) {
            var targetFolder = userFileService.getUserFileByIdAndUserId(targetId, userId, NORMAL);
            if (targetFolder == null || !UserFileItemTypeEnum.isFolder(targetFolder.getItemType())) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
            }
        }
    }

    /**
     * 检查是否移动到自身或子目录
     */
    private void validateNotMovingToSubfolder(List<UserFileDO> sourceFiles, Long targetId, Long userId) {
        var sourceFolderIds = new ArrayList<Long>();

        for (UserFileDO sourceFile : sourceFiles) {
            if (UserFileItemTypeEnum.isFolder(sourceFile.getItemType())) {
                sourceFolderIds.add(sourceFile.getId());
            }
        }

        if (CollUtil.isEmpty(sourceFolderIds)) {
            return;
        }

        // 检查是否移动到自身
        if (sourceFolderIds.contains(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_IS_ITSELF_OR_SUB);
        }

        // 检查是否移动到子目录
        var subfolderIds = userFileService.getAllSubfolders(userId, sourceFolderIds);
        if (CollUtil.isNotEmpty(subfolderIds) && subfolderIds.contains(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_IS_ITSELF_OR_SUB);
        }
    }

    /**
     * 检查重名
     */
    private void validateNoNameConflict(List<UserFileDO> sourceFiles, Long targetId, Long userId) {
        var sourceFileNames = new ArrayList<String>();
        var sourceFolderNames = new ArrayList<String>();

        for (UserFileDO sourceFile : sourceFiles) {
            if (UserFileItemTypeEnum.isFile(sourceFile.getItemType())) {
                sourceFileNames.add(sourceFile.getName());
            } else {
                sourceFolderNames.add(sourceFile.getName());
            }
        }

        // 校验文件重名
        if (CollUtil.isNotEmpty(sourceFileNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFileNames, userId, targetId, NORMAL, FILE);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FILE);
            }
        }

        // 校验文件夹重名
        if (CollUtil.isNotEmpty(sourceFolderNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFolderNames, userId, targetId, NORMAL, FOLDER);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FOLDER);
            }
        }
    }

    /**
     * 处理存储源迁移（如果需要）
     * 核心改进：异步标记，不阻塞用户操作
     */
    private void handleStorageMigrationIfNeeded(List<UserFileDO> sourceFiles, Long targetId, Long userId) {
        // 获取目标存储源ID
        Long targetStorageSourceId = getTargetStorageSourceId(targetId, userId);
        if (targetStorageSourceId == null) {
            return;
        }

        // 检查每个文件是否需要迁移
        for (UserFileDO sourceFile : sourceFiles) {
            // 只处理继承类型的文件/文件夹
            if (sourceFile.getStorageSourceType() != null && sourceFile.getStorageSourceType() == 1) {
                if (!Objects.equals(sourceFile.getStorageSourceId(), targetStorageSourceId)) {
                    // 标记为待迁移（异步处理）
                    migrationService.markForMigration(sourceFile.getId(), targetStorageSourceId, userId);
                    log.info("文件已标记为待迁移: fileId={}, targetStorageId={}", 
                            sourceFile.getId(), targetStorageSourceId);
                }
            }
        }
    }

    /**
     * 获取目标存储源ID
     */
    private Long getTargetStorageSourceId(Long targetId, Long userId) {
        if (targetId == 0) {
            var defaultStorage = storageSourceService.getDefaultStorageSource(userId);
            return defaultStorage != null ? defaultStorage.getId() : null;
        } else {
            var targetFolder = userFileService.getUserFileByIdAndUserId(targetId, userId, NORMAL);
            return targetFolder != null ? targetFolder.getStorageSourceId() : null;
        }
    }
}