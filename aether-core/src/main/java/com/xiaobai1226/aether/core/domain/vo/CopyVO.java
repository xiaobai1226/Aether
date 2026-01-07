package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Size;

import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_COPY_CONTENT_EMPTY;

/**
 * 复制文件实体类VO
 *
 * @author bai
 */
@Data
public class CopyVO {
    /**
     * 复制内容ID集合（数量必须大于0）
     */
    @NotNull(message = ERROR_COPY_CONTENT_EMPTY)
    @Size(min = 1, message = ERROR_COPY_CONTENT_EMPTY)
    private List<Long> sourceIds;

    /**
     * 目标文件夹路径
     */
    private String targetPath;
}
