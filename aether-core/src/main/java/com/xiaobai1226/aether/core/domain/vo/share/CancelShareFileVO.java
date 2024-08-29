package com.xiaobai1226.aether.core.domain.vo.share;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import static com.xiaobai1226.aether.core.constant.result.error.ResultShareErrMsgConsts.ERROR_SHARE_CANCEL_CONTENT_EMPTY;


/**
 * 取消分享文件实体类VO
 *
 * @author bai
 */
@Data
public class CancelShareFileVO {

    /**
     * ShareID集合，使用，分割的字符串
     */
    @NotBlank(message = ERROR_SHARE_CANCEL_CONTENT_EMPTY)
    private String ids;
}
