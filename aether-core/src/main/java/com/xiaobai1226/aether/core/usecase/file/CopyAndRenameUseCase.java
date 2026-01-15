package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.domain.dto.UserFolderDTO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.ArrayList;
import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件复制并重命名用例
 * 
 * 职责：复制单个文件/文件夹到指定目录并重命名
 * 
 * @author bai
 */
@Component
@Slf4j
public class CopyAndRenameUseCase {

    @Inject
    private UserFileService userFileService;

    @Inject
    private QuotaService quotaService;

    /**
     * 执行复制并重命名操作
     * 
     * @param sourceId     源文件/文件夹ID
     * @param targetFolder 目标文件夹DTO
     * @param newName      新文件名
     * @param userId       用户ID
     */
    @Tran
    public void execute(Long sourceId, UserFolderDTO targetFolder, String newName, Long userId) {
        log.info("开始复制并重命名文件: sourceId={}, targetId={}, newName={}, userId={}",
                sourceId, targetFolder.getId(), newName, userId);

        // 1. 查找源文件树
        var sourceFileTreeList = userFileService.getUserFileTreeListByIds(List.of(sourceId), userId, NORMAL);

        if (CollUtil.isEmpty(sourceFileTreeList)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_CONTENT_EMPTY);
        }

        var sourceFileTree = sourceFileTreeList.get(0);

        // 2. 检查新名称是否与源文件名相同（仅记录日志，不阻止）
        if (sourceFileTree.getName().equals(newName)) {
            log.warn("新名称与源文件名相同，建议使用普通复制: sourceId={}, name={}", sourceId, newName);
        }

        // 3. 检查是否复制到自身或子目录（如果是文件夹）
        if (UserFileItemTypeEnum.isFolder(sourceFileTree.getItemType())) {
            validateNotCopyingToSubfolder(sourceFileTree, targetFolder.getId(), userId);
        }

        // 4. 检查新名称是否重名（使用 newName）
        validateNoNameConflict(newName, sourceFileTree.getItemType(), targetFolder.getId(), userId);

        // 5. 递归获取完整文件树
        userFileService.getSubUserFileTree(userId, sourceFileTreeList);

        // 6. TODO 计算总占用空间
        // var totalSize =
        // userFileService.getUserFileTreeSpaceUsage(sourceFileTreeList);

        // 7. 检查配额（可选，目前注释）
        // quotaService.checkEnough(userId, totalSize);

        // 8. 执行复制并重命名（传入 newName）
        userFileService.copyWithNewName(targetFolder, userId, sourceFileTree, newName, 0L);

        log.info("文件复制并重命名完成: sourceId={}, targetId={}, newName={}, totalSize={}",
                sourceId, targetFolder.getId(), newName, 0);
    }

    /**
     * 检查是否复制到自身或子目录
     */
    private void validateNotCopyingToSubfolder(UserFileTreeDTO sourceFileTree, Long targetId, Long userId) {
        // 检查是否复制到自身
        if (sourceFileTree.getId().equals(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
        }

        // 检查是否复制到子目录
        var subfolderIds = userFileService.getAllSubfolders(userId, List.of(sourceFileTree.getId()));
        if (CollUtil.isNotEmpty(subfolderIds) && subfolderIds.contains(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
        }
    }

    /**
     * 检查新名称是否重名
     * 
     * @param newName  新文件名
     * @param itemType 文件类型
     * @param targetId 目标文件夹ID
     * @param userId   用户ID
     */
    private void validateNoNameConflict(String newName, Integer itemType, Long targetId, Long userId) {
        var existNameCount = userFileService.getCountByNames(
                List.of(newName),
                userId,
                targetId,
                NORMAL,
                UserFileItemTypeEnum.getEnumByFlag(itemType));

        if (existNameCount > 0) {
            if (UserFileItemTypeEnum.isFile(itemType)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FILE);
            } else {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FOLDER);
            }
        }
    }
}