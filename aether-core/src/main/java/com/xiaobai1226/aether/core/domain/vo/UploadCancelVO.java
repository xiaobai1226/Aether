package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_TASK_ID_EMPTY;

/**
 * 取消上传任务 VO
 */
@Data
public class UploadCancelVO {

    /**
     * 任务ID
     */
    @NotBlank(message = ERROR_TASK_ID_EMPTY)
    private String taskId;
}