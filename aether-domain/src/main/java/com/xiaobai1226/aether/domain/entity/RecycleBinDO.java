package com.xiaobai1226.aether.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 回收站实体类DO
 *
 * @author bai
 */
@TableName("recycle_bin")
@Data
public class RecycleBinDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 回收ID
     */
    private String recycleId;

    /**
     * 是否为顶层目录 0 否 1 是
     */
    @TableField("is_root")
    private Integer root;

    /**
     * 用户文件ID
     */
    private Integer userFileId;

    /**
     * 所属用户ID
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private String createTime;
}