package com.xiaobai1226.aether.dao.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 存储源实体类DO
 *
 * @author bai
 */
@Data
@Accessors(chain = true)
@TableName("storage_source")
public class StorageSourceDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 存储源名称
     */
    private String name;

    /**
     * 存储源类型 0=本地存储
     */
    private Integer type;

    /**
     * 存储路径（本地绝对路径）
     */
    private String path;

    /**
     * 是否为默认存储源 0=否 1=是
     */
    private Integer isDefault;

    /**
     * 状态 0=禁用 1=启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}