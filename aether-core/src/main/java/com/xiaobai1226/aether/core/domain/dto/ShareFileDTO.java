package com.xiaobai1226.aether.core.domain.dto;

import com.xiaobai1226.aether.core.domain.entity.ShareDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分享文件实体类DTO
 * TODO 冗余字段过多，重新设计
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShareFileDTO extends ShareDO {

    /**
     * 文件名称
     */
    private String name;

    /**
     * 父目录ID，0为根目录
     */
    private Integer parentId;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件状态
     */
    private Integer fileStatus;

    /**
     * 文件类型 0 目录 1 文件
     */
    private Integer itemType;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 文件分类 0 其他 1 视频 2音频 3 图片 4 文档
     */
    private Integer category;
}