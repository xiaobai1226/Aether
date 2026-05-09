package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;

/**
 * 设置文件夹存储源请求VO
 *
 * @author bai
 */
@Data
public class SetFolderStorageSourceVO {
    /**
     * 文件夹ID
     */
    @NotNull(message = ERROR_FOLDER_ID_EMPTY)
    private Long folderId;

    /**
     * 新的存储源ID
     */
    @NotNull(message = ERROR_STORAGE_SOURCE_ID_EMPTY)
    private Long storageSourceId;
}

