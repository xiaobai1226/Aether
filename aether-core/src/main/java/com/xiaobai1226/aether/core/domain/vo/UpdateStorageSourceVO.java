package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;

/**
 * 更新存储源请求VO
 *
 * @author bai
 */
@Data
public class UpdateStorageSourceVO {
    /**
     * 存储源ID
     */
    @NotNull(message = ERROR_STORAGE_SOURCE_ID_EMPTY)
    private Long id;

    /**
     * 存储源名称
     */
    @NotBlank(message = ERROR_STORAGE_SOURCE_NAME_EMPTY)
    private String name;
}

