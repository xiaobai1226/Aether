package com.xiaobai1226.aether.core.domain.dto;

import com.xiaobai1226.aether.core.domain.entity.UserFileDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户文件实体类DTO
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFileDTO extends UserFileDO {

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * md5唯一标识
     */
    private String identifier;

    /**
     * 文件所在路径
     */
    private String path;
}