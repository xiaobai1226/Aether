package com.xiaobai1226.aether.core.domain.vo.share;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 校验提取码实体类VO
 *
 * @author bai
 */
@Data
public class CheckExtractionCodeVO {

    /**
     * 提取码，4位字符串，只包含字母数字
     * TODO 增加格式校验
     */
    @NotNull(message = "提取码不能为空")
    private String extractionCode;

    /**
     * 分享内容ID
     */
    @NotNull(message = "分享ID不能为空")
    private String shareId;
}
