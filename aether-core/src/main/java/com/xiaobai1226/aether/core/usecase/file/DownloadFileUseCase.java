package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.cache.DownloadCache;
import com.xiaobai1226.aether.core.domain.dto.DownloadFileDTO;
import com.xiaobai1226.aether.core.domain.dto.DownloadLocalFileDTO;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.support.UserFileDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.DownloadedFile;

import java.io.IOException;
import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DOWNLOAD_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_NO_EXIST;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_SIGN;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件下载用例
 * 
 * 职责：创建下载链接和处理文件下载
 * 
 * @author bai
 */
@Component
@Slf4j
public class DownloadFileUseCase {

    @Inject
    private UserFileService userFileService;

    @Inject
    private DownloadCache downloadCache;

    @Inject
    private UserFileDownloadService userFileDownloadService;

    /**
     * 创建下载签名
     * 
     * @param fileIds 文件ID列表
     * @param userId  用户ID
     * @return 下载签名
     */
    public String createDownloadSign(List<Long> fileIds, Long userId) {
        log.info("创建下载签名: fileIds={}, userId={}", fileIds, userId);

        // 1. 校验文件
        if (CollUtil.isEmpty(fileIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_CONTENT_EMPTY);
        }

        var userFileDTOList = userFileService.getUserFileDTOListByIds(fileIds, userId, NORMAL);
        if (CollUtil.isEmpty(userFileDTOList) || userFileDTOList.size() != fileIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 2. 生成签名并缓存
        String sign = RandomUtil.randomString(20);
        downloadCache.setDownloadSign(new DownloadFileDTO(fileIds, userId), sign);

        log.info("下载签名创建成功: sign={}", sign);
        return sign;
    }

    /**
     * 通过签名下载文件
     * 
     * @param sign 下载签名
     * @return 下载文件对象
     * @throws IOException IO异常
     */
    public DownloadedFile downloadBySign(String sign) throws IOException {
        log.info("开始下载文件: sign={}", sign);

        // 1. 从缓存获取下载信息
        var downloadFileDTO = downloadCache.getDownloadInfo(sign);
        if (downloadFileDTO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SIGN);
        }

        // 2. 获取文件树
        var userFileTreeDTOList = userFileService.getUserFileTreeListByIds(
                downloadFileDTO.getIds(),
                downloadFileDTO.getUserId(),
                NORMAL
        );

        if (CollUtil.isEmpty(userFileTreeDTOList) || 
            userFileTreeDTOList.size() != downloadFileDTO.getIds().size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 3. 递归获取子文件
        userFileService.getSubUserFileTree(downloadFileDTO.getUserId(), userFileTreeDTOList);

        // 4. 执行下载
        var downloadedFile = userFileService.download(userFileTreeDTOList, downloadFileDTO.getUserId());

        log.info("文件下载成功: fileCount={}", userFileTreeDTOList.size());
        return downloadedFile;
    }

    /**
     * 解析单文件本地下载信息（用于Range断点续传）
     */
    public DownloadLocalFileDTO resolveSingleLocalFileBySign(String sign) {
        var downloadFileDTO = downloadCache.getDownloadInfo(sign);
        if (downloadFileDTO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SIGN);
        }

        if (downloadFileDTO.getIds() == null || downloadFileDTO.getIds().size() != 1) {
            return null;
        }

        var userFileTreeDTOList = userFileService.getUserFileTreeListByIds(
                downloadFileDTO.getIds(),
                downloadFileDTO.getUserId(),
                NORMAL
        );
        if (CollUtil.isEmpty(userFileTreeDTOList) || userFileTreeDTOList.size() != 1) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        var node = userFileTreeDTOList.getFirst();
        if (!com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.isFile(node.getItemType())) {
            return null;
        }
        return userFileDownloadService.resolveSingleLocalFile(node, downloadFileDTO.getUserId());
    }
}