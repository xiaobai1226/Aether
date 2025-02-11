package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_FILE_NAME;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.constant.VerifyConsts.FILE_NAME_MAX_LENGTH;
import static com.xiaobai1226.aether.common.constant.VerifyConsts.FILE_NAME_MIN_LENGTH;

/**
 * 文件重命名VO
 */
@Data
public class FileRenameVO {

    /**
     * 用户文件ID
     */
    @NotNull(message = ERROR_FILE_FOLDER_ID_EMPTY)
    private Integer id;

    /**
     * 新名称
     */
    @NotBlank(message = ERROR_FILE_NAME_EMPTY)
    @Length(min = FILE_NAME_MIN_LENGTH, max = FILE_NAME_MAX_LENGTH, message = ERROR_FILE_NAME_LENGTH)
    @Pattern(value = REGEX_FILE_NAME, message = ERROR_FILE_NAME_FORMAT)
    private String newName;
}
