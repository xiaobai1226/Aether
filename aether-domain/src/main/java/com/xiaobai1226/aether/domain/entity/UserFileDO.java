package com.xiaobai1226.aether.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户文件实体类DO
 *
 * @author bai
 */
@Data
@Accessors(chain = true)
@TableName("user_file")
public class UserFileDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 父目录ID，0为根目录
     */
    private Integer parentId;

    /**
     * 文件ID，NULL为空文件，文件夹此列一定为空
     */
    private Integer fileId;

    /**
     * 所属用户ID
     */
    private Integer userId;

    /**
     * 文件类型 0 目录 1 文件
     */
    private Integer itemType;

    /**
     * 文件状态 1 正常 0 回收站
     */
    private Integer fileStatus;

    /**
     * 文件夹或文件名称
     */
    private String name;

    /**
     * 文件分类 0 其他 1 视频 2音频 3 图片 4 文档
     */
    private Integer category;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}