package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_COPY_CONTENT_EMPTY;

/**
 * 复制文件实体类VO
 *
 * @author bai
 */
@Data
public class CopyVO {
    /**
     * 复制内容ID集合，使用，分割的字符串
     */
    @NotNull(message = ERROR_COPY_CONTENT_EMPTY)
    private String sourceIds;

    /**
     * 目标文件夹路径
     */
    private String targetPath;
}
