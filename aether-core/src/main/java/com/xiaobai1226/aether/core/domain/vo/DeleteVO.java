package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;

/**
 * 删除文件实体类VO
 *
 * @author bai
 */
@Data
public class DeleteVO {
    /**
     * 删除内容ID集合，使用，分割的字符串
     */
    @NotNull(message = ERROR_DEL_CONTENT_EMPTY)
    private String ids;
}
