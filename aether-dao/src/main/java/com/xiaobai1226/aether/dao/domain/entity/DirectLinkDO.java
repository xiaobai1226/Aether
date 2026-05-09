package com.xiaobai1226.aether.dao.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件直链实体类DO
 */
@Data
@Accessors(chain = true)
@TableName("direct_link")
public class DirectLinkDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 直链token
     */
    private String token;

    /**
     * 用户文件ID
     */
    private Long userFileId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 状态 1 启用 0 失效
     */
    private Integer status;

    /**
     * 过期时间，NULL为永久
     */
    private String expireAt;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}