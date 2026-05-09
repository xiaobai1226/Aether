package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DIRECT_LINK_EXPIRE_DAYS_INVALID;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_SIGN;

/**
 * 更新直链有效期请求VO
 */
@Data
public class UpdateDirectLinkExpireVO {
    /**
     * 直链token
     */
    @NotBlank(message = ERROR_SIGN)
    private String token;

    /**
     * 直链有效期（天），0为永久
     */
    @NotNull(message = ERROR_DIRECT_LINK_EXPIRE_DAYS_INVALID)
    private Integer expireDays;
}
