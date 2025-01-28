package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_RESTORE_CONTENT_EMPTY;

/**
 * 还原回收站文件实体类VO
 *
 * @author bai
 */
@Data
public class RestoreRecycleBinVO {
    /**
     * 还原内容ID集合，使用，分割的字符串
     */
    @NotBlank(message = ERROR_RESTORE_CONTENT_EMPTY)
    private String recycleIds;
}
