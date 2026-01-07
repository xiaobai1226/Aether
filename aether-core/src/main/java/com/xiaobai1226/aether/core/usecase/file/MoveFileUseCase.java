package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.domain.dto.UserFolderDTO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
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

    @Db
    private UserFileMapper userFileMapper;

    /**
     * 执行移动操作
     * 
     * @param sourceIds    源文件/文件夹ID列表
     * @param targetFolder 目标文件夹DTO（可选，包含存储源信息，避免重复查询）
     * @param userId       用户ID
     */
    @Tran
    public void execute(List<Long> sourceIds, UserFolderDTO targetFolder, Long userId) {
        log.info("开始移动文件: sourceIds={}, targetId={}, userId={}", sourceIds, targetFolder.getId(), userId);

        // 1. 校验源文件
        List<UserFileDO> sourceFiles = userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL);
        if (CollUtil.isEmpty(sourceFiles) || sourceFiles.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_CONTENT_EMPTY);
        }

        // 3. 检查是否移动到当前目录
        if (Objects.equals(sourceFiles.get(0).getParentId(), targetFolder.getId())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_IN_CURRENT_FOLDER);
        }

        // 4. 检查是否移动到自身或子目录
        validateNotMovingToSubfolder(sourceFiles, targetFolder.getId(), userId);

        // 5. 检查重名
        validateNoNameConflict(sourceFiles, targetFolder.getId(), userId);

        List<Long> needStorageMigrationFileIds = new ArrayList<>();
        List<Long> needStorageMigrationFolderIds = new ArrayList<>();

        sourceFiles.forEach(sourceFile -> {
            // 如果是文件，且存储源与目标文件夹不一致，则需要迁移存储源
            if (UserFileItemTypeEnum.isFile(sourceFile.getItemType())
                    && sourceFile.getStorageSourceId() != targetFolder.getStorageSourceId()) {
                needStorageMigrationFileIds.add(sourceFile.getId());
            } else if (UserFileItemTypeEnum.isFolder(sourceFile.getItemType())
                    && sourceFile.getStorageSourceType() == 1
                    && sourceFile.getStorageSourceId() != targetFolder.getStorageSourceId()) { // 如果是文件夹，且存储源为继承父目录，且存储源与目标文件夹不一致，则需要迁移存储源
                needStorageMigrationFolderIds.add(sourceFile.getId());
            }
        });

        if (CollUtil.isNotEmpty(needStorageMigrationFolderIds)) {
            // 递归收集需要迁移的子文件和子文件夹ID
            List<Long> tempFolderIds = new ArrayList<>(needStorageMigrationFolderIds);
            for (Long folderId : tempFolderIds) {
                collectInheritedFileAndFolderIds(folderId, userId, needStorageMigrationFolderIds,
                        needStorageMigrationFileIds);
            }
        }

        // 6. 更新父目录
        userFileService.updateParentIdByIds(sourceIds, targetFolder.getId(), userId, NORMAL);

        // 7. 批量更新文件夹存储源（文件夹没有实际数据，只需更新元数据）
        if (CollUtil.isNotEmpty(needStorageMigrationFolderIds)) {
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .lambda()
                    .in(UserFileDO::getId, needStorageMigrationFolderIds)
                    .set(UserFileDO::getStorageSourceId, targetFolder.getStorageSourceId()));
            log.info("批量更新文件夹存储源完成: count={}, targetStorageId={}",
                    needStorageMigrationFolderIds.size(), targetFolder.getStorageSourceId());
        }

        // 8. 批量更新文件存储源并标记迁移（文件有实际数据，需要异步迁移）
        if (CollUtil.isNotEmpty(needStorageMigrationFileIds)) {
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .lambda()
                    .in(UserFileDO::getId, needStorageMigrationFileIds)
                    .set(UserFileDO::getStorageSourceId, targetFolder.getStorageSourceId())
                    .set(UserFileDO::getMigrationPending, 1));
            log.info("批量标记文件迁移完成: count={}, targetStorageId={}",
                    needStorageMigrationFileIds.size(), targetFolder.getStorageSourceId());
        }

        log.info("文件移动完成: sourceIds={}, targetId={}, 文件夹数={}, 文件数={}",
                sourceIds, targetFolder.getId(), needStorageMigrationFolderIds.size(),
                needStorageMigrationFileIds.size());
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
     * 递归收集需要迁移的文件和文件夹ID
     * 
     * 逻辑：
     * - 显式指定存储源（storageSourceType=2）的文件夹：跳过该项及其所有子项
     * - 继承存储源（storageSourceType=1 或 null）的文件夹：将ID加入文件夹列表，递归处理子项
     * - 继承存储源（storageSourceType=1 或 null）的文件：将ID加入文件列表
     * 
     * @param folderId                      文件夹ID
     * @param userId                        用户ID
     * @param needStorageMigrationFolderIds 需要迁移的文件夹ID列表（输出参数）
     * @param needStorageMigrationFileIds   需要迁移的文件ID列表（输出参数）
     */
    private void collectInheritedFileAndFolderIds(Long folderId, Long userId,
            List<Long> needStorageMigrationFolderIds, List<Long> needStorageMigrationFileIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var children = lambdaQuery
                .eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getParentId, folderId)
                .eq(UserFileDO::getFileStatus, NORMAL.flag())
                .list();

        if (CollUtil.isEmpty(children)) {
            return;
        }

        for (var child : children) {
            // 处理继承类型的项（storageSourceType=1 或 null）
            if (UserFileItemTypeEnum.isFolder(child.getItemType())) {
                // 跳过显式指定存储源的文件夹（storageSourceType=2）
                if (child.getStorageSourceType() != null && child.getStorageSourceType() == 2) {
                    log.debug("跳过显式指定存储源的项: id={}, name={}", child.getId(), child.getName());
                    continue; // 跳过该项及其所有子项
                }

                // 文件夹：加入列表，递归处理子项
                needStorageMigrationFolderIds.add(child.getId());
                collectInheritedFileAndFolderIds(child.getId(), userId, needStorageMigrationFolderIds,
                        needStorageMigrationFileIds);
            } else {
                // 文件：加入列表
                needStorageMigrationFileIds.add(child.getId());
            }
        }
    }
}