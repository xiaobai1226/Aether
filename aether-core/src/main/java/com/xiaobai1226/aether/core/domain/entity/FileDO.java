package com.xiaobai1226.aether.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文件实体类DO
 *
 * @author bai
 */
@TableName("file")
@Data
public class FileDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 缩略图 只有视频与图片存在
     */
    private String thumbnail;

    /**
     * 文件状态 0 转码中 1 转码成功 2 转码失败
     */
//    private Integer fileStatus;

    /**
     * 文件类型ID
     */
    private Integer fileType;

    /**
     * md5唯一标识
     */
    private String identifier;

    /**
     * 存储源ID 1 本地存储
     */
//    private Integer storageSourceId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;
}