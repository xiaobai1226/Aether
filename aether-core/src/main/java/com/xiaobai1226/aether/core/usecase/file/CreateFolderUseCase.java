package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.domain.dto.UserFolderDTO;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NO_EXIST;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;

/**
 * 创建文件夹用例
 * 
 * 职责：创建新文件夹
 * 
 * @author bai
 */
@Component
@Slf4j
public class CreateFolderUseCase {

    @Inject
    private UserFileService userFileService;

    /**
     * 执行创建文件夹操作
     * 
     * @param folderName 文件夹名称
     * @param parentPath 父目录路径（可为null表示根目录）
     * @param userId     用户ID
     * @return 创建的文件夹对象
     */
    @Tran
    public UserFileDO execute(String folderName, String parentPath, Long userId) {
        log.info("开始创建文件夹: folderName={}, parentPath={}, userId={}", folderName, parentPath, userId);

        // 1. 使用 getFolderDTO 获取父文件夹（包含存储源信息）
        UserFolderDTO parentFolder = userFileService.getFolderDTO(userId, parentPath);
        if (parentFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 2. 创建文件夹（包含重名检查）
        var newFolder = userFileService.newFolder(folderName, parentFolder, userId);

        log.info("文件夹创建成功: folderId={}, folderName={}", newFolder.getId(), folderName);
        return newFolder;
    }
}