package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 删除文件到回收站用例
 * 
 * 职责：将文件/文件夹删除到回收站（软删除）
 * 
 * @author bai
 */
@Component
@Slf4j
public class DeleteFileUseCase {

    @Inject
    private UserFileService userFileService;

    /**
     * 执行删除操作（移动到回收站）
     * 
     * @param ids    要删除的文件/文件夹ID列表
     * @param userId 用户ID
     */
    @Tran
    public void execute(List<Long> ids, Long userId) {
        log.info("开始删除文件到回收站: ids={}, userId={}", ids, userId);

        // 1. 查找要删除的文件/文件夹
        var deleteFileTreeList = userFileService.getUserFileTreeListByIds(ids, userId, NORMAL);

        if (CollUtil.isEmpty(deleteFileTreeList) || deleteFileTreeList.size() != ids.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DEL_CONTENT_EMPTY);
        }

        // 2. 递归获取文件树（包括所有子文件）
        userFileService.getSubUserFileTree(userId, deleteFileTreeList);

        // 3. 执行删除（更新状态为回收站+创建回收站记录）
        userFileService.delete(deleteFileTreeList, userId);

        log.info("文件已删除到回收站: ids={}, 共{}个文件", ids, countTotalFiles(deleteFileTreeList));
    }

    /**
     * 统计总文件数（包括子文件）
     */
    private int countTotalFiles(List<UserFileTreeDTO> treeList) {
        int count = treeList.size();
        for (var tree : treeList) {
            if (CollUtil.isNotEmpty(tree.getChildUserFileDTOList())) {
                count += countTotalFiles(tree.getChildUserFileDTOList());
            }
        }
        return count;
    }
}