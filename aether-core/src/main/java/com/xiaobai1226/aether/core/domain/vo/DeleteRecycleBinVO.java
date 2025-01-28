package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;

/**
 * 删除回收站文件实体类VO
 *
 * @author bai
 */
@Data
public class DeleteRecycleBinVO {
    /**
     * 删除内容ID集合，使用，分割的字符串
     */
    @NotBlank(message = ERROR_DEL_CONTENT_EMPTY)
    private String recycleIds;
}
