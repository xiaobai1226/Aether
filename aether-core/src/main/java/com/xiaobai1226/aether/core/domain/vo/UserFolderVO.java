package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;

/**
 * 用户文件夹实体类VO
 *
 * @author bai
 */
@Data
public class UserFolderVO {

    /**
     * 所属文件夹路径
     */
    private String path;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页元素数
     */
    private Integer pageSize;
}