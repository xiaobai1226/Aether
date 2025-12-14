package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;

/**
 * 添加存储源请求VO
 *
 * @author bai
 */
@Data
public class AddStorageSourceVO {
    /**
     * 存储源名称
     */
    @NotBlank(message = ERROR_STORAGE_SOURCE_NAME_EMPTY)
    private String name;

    /**
     * 存储源类型 0=本地存储
     */
    @NotNull(message = ERROR_STORAGE_SOURCE_TYPE_EMPTY)
    private Integer type;

    /**
     * 存储路径
     */
    @NotBlank(message = ERROR_STORAGE_SOURCE_PATH_EMPTY)
    private String path;

    /**
     * 是否设为默认 0=否 1=是
     */
    private Integer isDefault;
}

