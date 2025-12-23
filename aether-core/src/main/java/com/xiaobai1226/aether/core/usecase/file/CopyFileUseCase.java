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
import java.util.Objects;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件复制用例
 * 
 * 职责：复制文件/文件夹到指定目录
 * 
 * @author bai
 */
@Component
@Slf4j
public class CopyFileUseCase {

    @Inject
    private UserFileService userFileService;

    @Inject
    private QuotaService quotaService;

    /**
     * 执行复制操作
     * 
     * @param sourceIds    源文件/文件夹ID列表
     * @param targetFolder 目标文件夹DTO（可选，避免重复查询）
     * @param userId       用户ID
     */
    @Tran
    public void execute(List<Long> sourceIds, UserFolderDTO targetFolder, Long userId) {
        log.info("开始复制文件: sourceIds={}, targetId={}, userId={}", sourceIds, targetFolder.getId(), userId);

        // 1. 查找源文件树
        var sourceFileTreeList = userFileService.getUserFileTreeListByIds(sourceIds, userId, NORMAL);
        
        if (CollUtil.isEmpty(sourceFileTreeList) || sourceFileTreeList.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_CONTENT_EMPTY);
        }

        // 3. 检查是否复制到当前目录
        if (Objects.equals(sourceFileTreeList.get(0).getParentId(), targetFolder.getId())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_IN_CURRENT_FOLDER);
        }

        // 4. 检查是否复制到自身或子目录
        validateNotCopyingToSubfolder(sourceFileTreeList, targetFolder.getId(), userId);

        // 5. 检查重名
        validateNoNameConflict(sourceFileTreeList, targetFolder.getId(), userId);

        // 6. 递归获取完整文件树
        userFileService.getSubUserFileTree(userId, sourceFileTreeList);

        // 7. 计算总占用空间
        var totalSize = userFileService.getUserFileTreeSpaceUsage(sourceFileTreeList);

        // 8. TODO 检查配额
        // quotaService.checkEnough(userId, totalSize);

        // 9. 执行复制
        userFileService.copy(targetFolder.getId(), userId, sourceFileTreeList, totalSize);

        log.info("文件复制完成: sourceIds={}, targetId={}, totalSize={}", sourceIds, targetFolder.getId(), totalSize);
    }

    /**
     * 检查是否复制到自身或子目录
     */
    private void validateNotCopyingToSubfolder(List<UserFileTreeDTO> sourceFileTreeList, Long targetId, Long userId) {
        var sourceFolderIds = new ArrayList<Long>();

        for (var sourceFile : sourceFileTreeList) {
            if (UserFileItemTypeEnum.isFolder(sourceFile.getItemType())) {
                sourceFolderIds.add(sourceFile.getId());
            }
        }

        if (CollUtil.isEmpty(sourceFolderIds)) {
            return;
        }

        // 检查是否复制到自身
        if (sourceFolderIds.contains(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
        }

        // 检查是否复制到子目录
        var subfolderIds = userFileService.getAllSubfolders(userId, sourceFolderIds);
        if (CollUtil.isNotEmpty(subfolderIds) && subfolderIds.contains(targetId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
        }
    }

    /**
     * 检查重名
     */
    private void validateNoNameConflict(List<UserFileTreeDTO> sourceFileTreeList, Long targetId, Long userId) {
        var sourceFileNames = new ArrayList<String>();
        var sourceFolderNames = new ArrayList<String>();

        for (var sourceFile : sourceFileTreeList) {
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
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FILE);
            }
        }

        // 校验文件夹重名
        if (CollUtil.isNotEmpty(sourceFolderNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFolderNames, userId, targetId, NORMAL, FOLDER);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FOLDER);
            }
        }
    }
}