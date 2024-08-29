package com.xiaobai1226.aether.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分享文件中间表实体类DO
 *
 * @author bai
 */
@TableName("share_user_file")
@Data
public class ShareUserFileDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分享ID
     */
    private String shareId;

    /**
     * 用户文件ID
     */
    private Integer userFileId;
}