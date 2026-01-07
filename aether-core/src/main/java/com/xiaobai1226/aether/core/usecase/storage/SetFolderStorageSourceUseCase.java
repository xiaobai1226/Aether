package com.xiaobai1226.aether.core.usecase.storage;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
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

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NO_EXIST;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_NO_STORAGE_SOURCE;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 设置文件夹存储源用例
 * 
 * 职责：设置文件夹的存储源并同步更新子文件
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

    @Db
    private UserFileMapper userFileMapper;

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

        if (folder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        if (!UserFileItemTypeEnum.isFolder(folder.getItemType())) {
            throw new FailResultException(PARAM_IS_INVALID, "只能为文件夹设置存储源");
        }

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

        // 4. 收集需要迁移的子文件和子文件夹
        List<Long> needStorageMigrationFileIds = new ArrayList<>();
        List<Long> needStorageMigrationFolderIds = new ArrayList<>();
        collectInheritedFileAndFolderIds(folderId, userId, needStorageMigrationFolderIds, needStorageMigrationFileIds);

        // 5. 批量更新文件夹存储源（文件夹没有实际数据，只需更新元数据）
        if (CollUtil.isNotEmpty(needStorageMigrationFolderIds)) {
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .lambda()
                    .in(UserFileDO::getId, needStorageMigrationFolderIds)
                    .set(UserFileDO::getStorageSourceId, storageSourceId));
            log.info("批量更新文件夹存储源完成: count={}, targetStorageId={}",
                    needStorageMigrationFolderIds.size(), storageSourceId);
        }

        // 6. 批量更新文件存储源并标记迁移（文件有实际数据，需要异步迁移）
        if (CollUtil.isNotEmpty(needStorageMigrationFileIds)) {
            userFileMapper.update(null, new UpdateWrapper<UserFileDO>()
                    .lambda()
                    .in(UserFileDO::getId, needStorageMigrationFileIds)
                    .set(UserFileDO::getStorageSourceId, storageSourceId)
                    .set(UserFileDO::getMigrationPending, 1));
            log.info("批量标记文件迁移完成: count={}, targetStorageId={}",
                    needStorageMigrationFileIds.size(), storageSourceId);
        }

        log.info("文件夹存储源设置完成: folderId={}, 文件夹数={}, 文件数={}",
                folderId, needStorageMigrationFolderIds.size(), needStorageMigrationFileIds.size());
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