package com.xiaobai1226.aether.core.usecase.file;

import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NAME_EXIST;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NO_EXIST;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_RENAME;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件重命名用例
 * 
 * 职责：处理文件/文件夹重命名逻辑
 * 
 * @author bai
 */
@Component
@Slf4j
public class RenameFileUseCase {

    @Inject
    private UserFileService userFileService;

    /**
     * 执行重命名操作
     * 
     * @param fileId  文件/文件夹ID
     * @param newName 新名称
     * @param userId  用户ID
     */
    @Tran
    public void execute(Long fileId, String newName, Long userId) {
        log.info("开始重命名文件: fileId={}, newName={}, userId={}", fileId, newName, userId);

        // 1. 校验文件是否存在
        var userFile = userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL);
        if (userFile == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 2. 检查新名称是否与同级文件重名
        var existingFile = userFileService.getUserFileByName(newName, userId, userFile.getParentId(), NORMAL);
        if (existingFile != null && !existingFile.getId().equals(fileId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NAME_EXIST);
        }

        // 3. 执行重命名
        var result = userFileService.rename(fileId, userId, newName, userFile, NORMAL);
        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RENAME);
        }

        log.info("文件重命名成功: fileId={}, oldName={}, newName={}", fileId, userFile.getName(), newName);
    }
}