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
     * 排序字段 0 文件名 1 创建时间 2 修改时间 3 文件大小
     */
    private Integer sortingField;

    /**
     * 排序顺序 0 升序 1 降序
     */
    private Integer sortingMethod;
}