package com.xiaobai1226.aether.core.usecase.storage;

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

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NO_EXIST;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_NO_STORAGE_SOURCE;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 设置文件夹存储源用例
 * 
 * 职责：设置文件夹的存储源并异步迁移子文件
 * 
 * @author bai
 */
@Component
@Slf4j
public class SetFolderStorageSourceUseCase {

    @Inject
    private UserFileService userFileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageMigrationService migrationService;

    /**
     * 执行设置存储源操作
     * 
     * @param folderId        文件夹ID
     * @param storageSourceId 新的存储源ID
     * @param userId          用户ID
     */
    @Tran
    public void execute(Long folderId, Long storageSourceId, Long userId) {
        log.info("开始设置文件夹存储源: folderId={}, storageSourceId={}, userId={}", 
                folderId, storageSourceId, userId);

        // 1. 校验文件夹
        var folder = userFileService.getUserFileByIdAndUserId(folderId, userId, NORMAL);
        validateFolder(folder);

        // 2. 校验存储源
        var storageSource = storageSourceService.getStorageSourceById(storageSourceId, userId);
        if (storageSource == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        // 3. 更新文件夹的存储源配置
        folder.setStorageSourceId(storageSourceId);
        folder.setStorageSourceType(2); // 显式指定
        userFileService.updateById(folder);

        log.info("文件夹存储源已更新: folderId={}, newStorageSourceId={}", folderId, storageSourceId);

        // 4. 标记子文件为待迁移（异步处理）
        migrationService.markFolderForMigration(folderId, storageSourceId, userId);

        log.info("文件夹存储源设置完成，子文件将在后台迁移: folderId={}", folderId);
    }

    /**
     * 校验文件夹
     */
    private void validateFolder(UserFileDO folder) {
        if (folder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        if (!UserFileItemTypeEnum.isFolder(folder.getItemType())) {
            throw new FailResultException(PARAM_IS_INVALID, "只能为文件夹设置存储源");
        }
    }
}