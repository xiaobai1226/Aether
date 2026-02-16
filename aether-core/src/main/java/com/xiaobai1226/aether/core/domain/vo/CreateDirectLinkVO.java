package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_FILE_ID_EMPTY;

/**
 * 创建文件直链请求VO
 */
@Data
public class CreateDirectLinkVO {
    /**
     * 用户文件ID
     */
    @NotNull(message = ERROR_FILE_ID_EMPTY)
    private Long id;

    /**
     * 直链有效期（天），0为永久
     */
    private Integer expireDays;
}