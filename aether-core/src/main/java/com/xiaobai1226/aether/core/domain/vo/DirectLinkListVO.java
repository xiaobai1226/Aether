package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_PAGE_NUM_EMPTY;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_PAGE_SIZE_EMPTY;

/**
 * 文件直链列表请求VO
 */
@Data
public class DirectLinkListVO {
    /**
     * 页码
     */
    @NotNull(message = ERROR_PAGE_NUM_EMPTY)
    private Integer pageNum;

    /**
     * 每页条数
     */
    @NotNull(message = ERROR_PAGE_SIZE_EMPTY)
    private Integer pageSize;
}
