package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.core.constant.RegexConsts.REGEX_FILE_NAME;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.core.constant.VerifyConsts.FILE_NAME_MAX_LENGTH;
import static com.xiaobai1226.aether.core.constant.VerifyConsts.FILE_NAME_MIN_LENGTH;

/**
 * 新建文件夹实体类VO
 *
 * @author bai
 */
@Data
public class NewFolderVO {

    /**
     * 文件夹名称
     */
    @NotBlank(message = ERROR_FILE_NAME_EMPTY)
    @Length(min = FILE_NAME_MIN_LENGTH, max = FILE_NAME_MAX_LENGTH, message = ERROR_FILE_NAME_LENGTH)
    @Pattern(value = REGEX_FILE_NAME, message = ERROR_FILE_NAME_FORMAT)
    private String folderName;

    /**
     * 所属文件夹路径
     */
    private String path;
}
