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
 * 上传文件实体类VO
 *
 * @author bai
 */
@Data
public class UploadFileVO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 所属文件夹路径
     */
    private String path;

    /**
     * 上传文件夹，文件所属文件夹路径
     */
    private String relativePath;

    /**
     * 文件名称
     */
    @NotNull(message = ERROR_FILE_NAME_EMPTY)
    @Length(min = FILE_NAME_MIN_LENGTH, max = FILE_NAME_MAX_LENGTH, message = ERROR_FILE_NAME_LENGTH)
    @Pattern(value = REGEX_FILE_NAME, message = ERROR_FILE_NAME_FORMAT)
    private String fileName;

    /**
     * 文件大小
     */
    @NotNull(message = ERROR_FILE_SIZE_EMPTY)
    private Long fileSize;

    /**
     * md5码
     */
    @NotBlank(message = ERROR_IDENTIFIER_EMPTY)
    private String identifier;

    /**
     * 切片索引
     */
    @NotNull(message = ERROR_CHUNK_INDEX_EMPTY)
    private Integer chunkIndex;

    /**
     * 总切片数
     */
    @NotNull(message = ERROR_TOTAL_CHUNKS_EMPTY)
    private Integer totalChunks;
}