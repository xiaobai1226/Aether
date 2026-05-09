package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.cache.DownloadTaskCache;
import com.xiaobai1226.aether.core.domain.dto.DownloadTaskDTO;
import com.xiaobai1226.aether.core.domain.dto.DownloadTaskFileDTO;
import com.xiaobai1226.aether.core.domain.dto.DownloadTaskStatusDTO;
import com.xiaobai1226.aether.core.enums.DownloadTaskStatusEnum;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.support.UserFileDownloadService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 下载任务用例
 */
@Component
@Slf4j
public class DownloadTaskUseCase {
    private static final Duration TASK_EXPIRE_DURATION = Duration.ofHours(2);
    private static final ExecutorService DOWNLOAD_TASK_EXECUTOR = Executors.newFixedThreadPool(2);

    @Inject
    private UserFileService userFileService;

    @Inject
    private UserFileDownloadService userFileDownloadService;

    @Inject
    private DownloadTaskCache downloadTaskCache;

    public String createDownloadTask(List<Long> fileIds, Long userId) {
        if (CollUtil.isEmpty(fileIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_CONTENT_EMPTY);
        }

        var userFileDTOList = userFileService.getUserFileDTOListByIds(fileIds, userId, NORMAL);
        if (CollUtil.isEmpty(userFileDTOList) || userFileDTOList.size() != fileIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        downloadTaskCache.cleanupExpiredTasks();

        String taskId = "dt_" + RandomUtil.randomString(20);
        DownloadTaskDTO taskDTO = new DownloadTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setUserId(userId);
        taskDTO.setIds(fileIds);
        taskDTO.setStatus(DownloadTaskStatusEnum.PENDING.flag());
        taskDTO.setProgress(0);
        taskDTO.setCreateTime(System.currentTimeMillis());
        taskDTO.setDownloaded(false);
        taskDTO.setExpireTime(System.currentTimeMillis() + TASK_EXPIRE_DURATION.toMillis());
        downloadTaskCache.setTask(taskDTO);

        DOWNLOAD_TASK_EXECUTOR.submit(() -> executeTask(taskId));
        return taskId;
    }

    public DownloadTaskStatusDTO getTaskStatus(String taskId, Long userId) {
        DownloadTaskDTO taskDTO = getTaskForUser(taskId, userId);
        return BeanUtil.copyProperties(taskDTO, DownloadTaskStatusDTO.class);
    }

    public DownloadTaskFileDTO downloadTaskFileBySign(String sign) {
        String taskId = downloadTaskCache.getTaskIdBySign(sign);
        if (taskId == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SIGN);
        }
        DownloadTaskDTO taskDTO = downloadTaskCache.getTask(taskId);
        if (taskDTO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_TASK_NOT_FOUND);
        }
        if (!DownloadTaskStatusEnum.SUCCEEDED.flag().equals(taskDTO.getStatus())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_TASK_NOT_READY);
        }
        if (taskDTO.getFilePath() == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_TASK_NOT_FOUND);
        }

        File file = new File(taskDTO.getFilePath());
        if (!file.exists()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_TASK_NOT_FOUND);
        }
        return new DownloadTaskFileDTO(taskId, file, taskDTO.getFileName());
    }

    private void executeTask(String taskId) {
        DownloadTaskDTO taskDTO = downloadTaskCache.getTask(taskId);
        if (taskDTO == null) {
            return;
        }
        try {
            taskDTO.setStatus(DownloadTaskStatusEnum.RUNNING.flag());
            taskDTO.setProgress(10);
            downloadTaskCache.setTask(taskDTO);

            var userFileTreeDTOList = userFileService.getUserFileTreeListByIds(taskDTO.getIds(), taskDTO.getUserId(), NORMAL);
            if (CollUtil.isEmpty(userFileTreeDTOList) || userFileTreeDTOList.size() != taskDTO.getIds().size()) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            taskDTO.setProgress(35);
            downloadTaskCache.setTask(taskDTO);

            userFileService.getSubUserFileTree(taskDTO.getUserId(), userFileTreeDTOList);

            int totalFileCount = countFileCount(userFileTreeDTOList);
            taskDTO.setTotalFileCount(totalFileCount);
            taskDTO.setCompletedFileCount(0);
            taskDTO.setProgress(60);
            downloadTaskCache.setTask(taskDTO);

            if (totalFileCount == 0) {
                taskDTO.setProgress(99);
                downloadTaskCache.setTask(taskDTO);
            }

            var packageFile = userFileDownloadService.buildPackageFile(userFileTreeDTOList, taskDTO.getUserId(),
                    packedBytes -> {
                        int completed = (taskDTO.getCompletedFileCount() == null ? 0 : taskDTO.getCompletedFileCount()) + 1;
                        taskDTO.setCompletedFileCount(completed);
                        if (taskDTO.getTotalFileCount() != null && taskDTO.getTotalFileCount() > 0) {
                            int progress = 60 + (int) Math.min(39, Math.round((completed * 39.0) / taskDTO.getTotalFileCount()));
                            taskDTO.setProgress(progress);
                        }
                        downloadTaskCache.setTask(taskDTO);
                    });
            taskDTO.setDownloadSign(RandomUtil.randomString(24));
            taskDTO.setStatus(DownloadTaskStatusEnum.SUCCEEDED.flag());
            taskDTO.setProgress(100);
            taskDTO.setFilePath(packageFile.getFile().getAbsolutePath());
            taskDTO.setFileName(packageFile.getFileName());
            taskDTO.setErrorMsg(null);
            downloadTaskCache.setTask(taskDTO);
        } catch (Exception e) {
            log.error("下载任务执行失败, taskId={}", taskId, e);
            taskDTO.setStatus(DownloadTaskStatusEnum.FAILED.flag());
            taskDTO.setProgress(100);
            taskDTO.setErrorMsg(ERROR_DOWNLOAD_TASK_FAILED);
            taskDTO.setFilePath(null);
            taskDTO.setFileName(null);
            downloadTaskCache.setTask(taskDTO);
        }
    }

    private int countFileCount(List<UserFileTreeDTO> userFileTreeDTOList) {
        if (CollUtil.isEmpty(userFileTreeDTOList)) {
            return 0;
        }
        int count = 0;
        for (var item : userFileTreeDTOList) {
            if (UserFileItemTypeEnum.isFile(item.getItemType())) {
                count++;
            } else if (CollUtil.isNotEmpty(item.getChildUserFileDTOList())) {
                count += countFileCount(item.getChildUserFileDTOList());
            }
        }
        return count;
    }

    private DownloadTaskDTO getTaskForUser(String taskId, Long userId) {
        DownloadTaskDTO taskDTO = downloadTaskCache.getTask(taskId);
        if (taskDTO == null || !userId.equals(taskDTO.getUserId())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_TASK_NOT_FOUND);
        }
        return taskDTO;
    }

    public void markTaskDownloaded(String taskId) {
        downloadTaskCache.markTaskDownloaded(taskId);
    }
}
