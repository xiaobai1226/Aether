package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 复制并重命名文件实体类VO
 *
 * @author bai
 */
@Data
public class CopyAndRenameVO {
    /**
     * 源文件ID
     */
    @NotNull(message = "源文件ID不能为空")
    private Long sourceId;

    /**
     * 目标文件夹路径
     */
    private String targetPath;

    /**
     * 新文件名
     */
    @NotBlank(message = "新文件名不能为空")
    private String newName;
}