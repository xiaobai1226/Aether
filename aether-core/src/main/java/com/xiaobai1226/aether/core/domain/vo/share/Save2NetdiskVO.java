package com.xiaobai1226.aether.core.domain.vo.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 保存分享文件到网盘实体类VO
 *
 * @author bai
 */
@Data
public class Save2NetdiskVO {

    /**
     * ShareID集合，使用，分割的字符串
     */
    @NotNull(message = "文件ID不能为空")
    @JsonProperty("ids")
    private String idsStr;

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
