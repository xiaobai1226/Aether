package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_SIGN;

/**
 * 撤销文件直链请求VO
 */
@Data
public class RevokeDirectLinkVO {
    /**
     * 直链token
     */
    @NotBlank(message = ERROR_SIGN)
    private String token;
}