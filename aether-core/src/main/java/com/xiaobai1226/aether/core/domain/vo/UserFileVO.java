package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;

/**
 * 用户文件实体类VO
 *
 * @author bai
 */
@Data
public class UserFileVO {

    /**
     * 分类ID
     */
    // TODO 增加分类校验
    private Integer category;

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

    /**
     * 排序字段 1 文件名 2 文件修改日期 3 文件大小
     */
    private Integer sortField;

    /**
     * 排序顺序 1 升序 2 降序
     */
    private Integer sortOrder;
}