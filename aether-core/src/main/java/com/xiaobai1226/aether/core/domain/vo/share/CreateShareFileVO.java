package com.xiaobai1226.aether.core.domain.vo.share;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_EXTRACTION_CODE;
import static com.xiaobai1226.aether.core.constant.result.error.ResultShareErrMsgConsts.*;

/**
 * 创建分享文件实体类VO
 *
 * @author bai
 */
@Data
public class CreateShareFileVO {

    /**
     * 提取码，4位字符串，只包含字母数字
     */
    @Pattern(value = REGEX_EXTRACTION_CODE, message = ERROR_SHARE_EXTRACTION_CODE_FORMAT)
    private String extractionCode;

    /**
     * 分享内容ID集合，使用，分割的字符串
     */
    @NotBlank(message = ERROR_SHARE_CONTENT_EMPTY)
    private String ids;

    /**
     * 有效期，单位 天，0为永久
     */
    @NotNull(message = ERROR_SHARE_VALIDITY_PERIOD_EMPTY)
    private Integer validityPeriod;
}
