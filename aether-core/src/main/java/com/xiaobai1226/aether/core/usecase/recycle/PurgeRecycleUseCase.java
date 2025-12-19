package com.xiaobai1226.aether.core.usecase.recycle;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.support.FilePurgeService;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.RecycleBinMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.*;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;

/**
 * 彻底删除回收站文件用例
 * 
 * 职责：从回收站彻底删除文件（不可恢复）
 * 
 * @author bai
 */
@Component
@Slf4j
public class PurgeRecycleUseCase {

    @Db
    private RecycleBinMapper recycleBinMapper;

    @Db
    private UserFileMapper userFileMapper;

    @Inject
    private QuotaService quotaService;

    @Inject
    private FilePurgeService filePurgeService;

    /**
     * 执行彻底删除操作
     * 
     * @param recycleIds 回收站ID列表（如果只有一个元素"-1"表示清空回收站）
     * @param userId     用户ID
     */
    @Tran
    public void execute(List<String> recycleIds, Long userId) {
        log.info("开始彻底删除文件: recycleIds={}, userId={}", recycleIds, userId);

        // 1. 查找回收站记录
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinWrapper = lambdaQuery.eq(RecycleBinDO::getUserId, userId);

        // 删除全部文件，如果只有一个元素，且为-1, 则为全部删除
        if (recycleIds.size() != 1 || !Objects.equals(recycleIds.get(0), "-1")) {
            recycleBinWrapper.in(RecycleBinDO::getRecycleId, recycleIds);
        }

        var recycleBinList = recycleBinWrapper.list();
        if (CollUtil.isEmpty(recycleBinList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_DEL_CONTENT_EMPTY);
        }

        // 2. 收集回收站ID和用户文件ID
        var recycleBinIds = new ArrayList<Long>();
        var userFileIds = new ArrayList<Long>();
        for (var recycleBin : recycleBinList) {
            userFileIds.add(recycleBin.getUserFileId());
            recycleBinIds.add(recycleBin.getId());
        }

        // 3. 删除回收站记录
        var delRecycleBinCount = recycleBinMapper.deleteBatchIds(recycleBinIds);
        if (delRecycleBinCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 4. 查询用户文件信息
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(DEL.flag());
        var userFileList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);

        if (CollUtil.isEmpty(userFileList) || recycleBinList.size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 5. 收集文件ID和计算总大小
        var fileIds = new HashSet<Long>();
        var totalSize = 0L;

        for (var userFile : userFileList) {
            if (UserFileItemTypeEnum.isFile(userFile.getItemType()) && userFile.getFileId() != null) {
                fileIds.add(userFile.getFileId());
                totalSize += userFile.getSize();
            }
        }

        // 6. 删除用户文件记录
        var delUserFileCount = userFileMapper.deleteBatchIds(userFileIds);
        if (delUserFileCount != userFileIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 7. 释放配额（QuotaService 内部已处理 bytes <= 0 的情况）
        quotaService.decreaseUsed(userId, totalSize);

        // 8. 删除物理文件（FilePurgeService 会检查引用计数，只删除没有被引用的文件）
        if (CollUtil.isNotEmpty(fileIds)) {
            filePurgeService.purgeFiles(userId, fileIds);
        }

        log.info("文件彻底删除完成: 共删除{}个UserFile, 释放空间{}字节", userFileIds.size(), totalSize);
    }
}