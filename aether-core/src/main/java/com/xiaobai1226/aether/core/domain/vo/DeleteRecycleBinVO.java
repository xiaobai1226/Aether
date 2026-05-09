package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Size;

import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;

/**
 * 删除回收站文件实体类VO
 *
 * @author bai
 */
@Data
public class DeleteRecycleBinVO {
    /**
     * 删除内容ID集合（数量必须大于0）
     */
    @NotNull(message = ERROR_DEL_CONTENT_EMPTY)
    @Size(min = 1, message = ERROR_DEL_CONTENT_EMPTY)
    private List<String> recycleIds;
}