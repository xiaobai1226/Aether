package com.xiaobai1226.aether.core.domain.vo.share;

import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 分页获取分享文件详情列表实体类VO
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetShareFileInfoListVO extends PageVO {
    /**
     * 分享内容ID
     */
    @NotNull(message = "分享ID不能为空")
    private String shareId;

    /**
     * 所属文件夹路径
     */
    private String path;
}
