package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_MOVE_CONTENT_EMPTY;

/**
 * 移动文件实体类VO
 *
 * @author bai
 */
@Data
public class MoveVO {
    /**
     * 移动内容ID集合，使用，分割的字符串
     */
    @NotNull(message = ERROR_MOVE_CONTENT_EMPTY)
    private String sourceIds;

    /**
     * 目标文件夹路径
     */
    private String targetPath;
}
