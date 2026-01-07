package com.xiaobai1226.aether.core.usecase.recycle;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.RecycleBinMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.*;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_RESTORE_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 回收站还原用例
 * 
 * 职责：从回收站还原文件/文件夹
 * 
 * @author bai
 */
@Component
@Slf4j
public class RestoreFileUseCase {

    @Db
    private RecycleBinMapper recycleBinMapper;

    @Inject
    private UserFileService userFileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageMigrationService migrationService;

    /**
     * 执行还原操作
     * 
     * @param recycleIds 回收站ID列表
     * @param userId     用户ID
     */
    @Tran
    public void execute(List<String> recycleIds, Long userId) {
        log.info("开始还原文件: recycleIds={}, userId={}", recycleIds, userId);

        // 1. 查找回收站记录
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinList = lambdaQuery
                .eq(RecycleBinDO::getUserId, userId)
                .in(RecycleBinDO::getRecycleId, recycleIds)
                .list();

        if (CollUtil.isEmpty(recycleBinList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RESTORE_CONTENT_EMPTY);
        }

        // 2. 收集回收站ID和用户文件ID
        var recycleBinIds = new ArrayList<Long>();
        var userFileIds = new ArrayList<Long>();
        var rootUserFileIdSet = new HashSet<Long>();

        for (var recycleBin : recycleBinList) {
            userFileIds.add(recycleBin.getUserFileId());
            recycleBinIds.add(recycleBin.getId());
            if (recycleBin.getRoot() == 1) {
                rootUserFileIdSet.add(recycleBin.getUserFileId());
            }
        }

        // 3. 删除回收站记录
        var delCount = recycleBinMapper.deleteByIds(recycleBinIds);
        if (delCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 4. 查询用户文件信息
        var lambdaQuery2 = new LambdaQueryChainWrapper<>(userFileService.getBaseMapper());
        var userFileList = lambdaQuery2
                .eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getFileStatus, DEL.flag())
                .in(UserFileDO::getId, userFileIds)
                .list();

        if (CollUtil.isEmpty(userFileList) || recycleBinList.size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 5. 处理重名和父目录检查
        var restoreNameSet = new HashSet<String>();
        for (var userFile : userFileList) {
            if (!rootUserFileIdSet.contains(userFile.getId())) {
                continue;
            }

            handleRestore(userFile, userId, restoreNameSet);
        }

        // 6. 更新文件状态为正常
        userFileService.updateUserFileStatusById(userFileIds, userId, NORMAL);

        // 7. 处理存储源迁移（如果需要）
        handleStorageMigration(userFileList, rootUserFileIdSet, userId);

        log.info("文件还原完成: 共还原{}个文件", userFileIds.size());
    }

    /**
     * 处理单个文件还原（检查父目录和重名）
     */
    private void handleRestore(UserFileDO userFile, Long userId, Set<String> restoreNameSet) {
        // 检查父目录是否存在
        Long parentId = userFile.getParentId();
        if (parentId != 0) {
            var parent = userFileService.getUserFileByIdAndUserId(parentId, userId, NORMAL);
            if (parent == null) {
                // 父目录不存在，移到根目录
                parentId = 0L;
                userFileService.updateParentIdByIds(List.of(userFile.getId()), 0L, userId, DEL);
            }
        }

        // 检查重名
        var checkName = parentId + "-" + userFile.getName();
        boolean needRename = false;

        if (restoreNameSet.contains(checkName)) {
            needRename = true;
        } else {
            var sameNameFile = userFileService.getUserFileByName(
                    userFile.getName(), userId, parentId, NORMAL);
            if (sameNameFile != null) {
                needRename = true;
            }
        }

        // 如果重名则改名
        if (needRename) {
            var newName = FileUtils.rename(userFile.getName());
            var result = userFileService.updateFileNameById(userFile.getId(), userId, newName, DEL);
            if (!result) {
                throw new FailResultException(SYSTEM_ERROR);
            }
            restoreNameSet.add(parentId + "-" + newName);
        } else {
            restoreNameSet.add(checkName);
        }
    }

    /**
     * 处理存储源迁移（如果需要）
     */
    private void handleStorageMigration(List<UserFileDO> userFileList,
            Set<Long> rootUserFileIdSet,
            Long userId) {
        for (var userFile : userFileList) {
            if (!rootUserFileIdSet.contains(userFile.getId())) {
                continue;
            }

            // 只处理继承类型的文件/文件夹
            if (userFile.getStorageSourceType() == null || userFile.getStorageSourceType() != 1) {
                continue;
            }

            // 获取目标存储源ID
            Long targetStorageSourceId;
            if (userFile.getParentId() == 0) {
                var defaultStorage = storageSourceService.getDefaultStorageSource(userId);
                if (defaultStorage == null) {
                    continue;
                }
                targetStorageSourceId = defaultStorage.getId();
            } else {
                var parent = userFileService.getUserFileByIdAndUserId(userFile.getParentId(), userId, NORMAL);
                if (parent == null || parent.getStorageSourceId() == null) {
                    continue;
                }
                targetStorageSourceId = parent.getStorageSourceId();
            }

            // 如果存储源不一致，标记为待迁移
            if (!Objects.equals(userFile.getStorageSourceId(), targetStorageSourceId)) {
                migrationService.markForMigration(userFile.getId(), targetStorageSourceId, userId);
            }
        }
    }
}