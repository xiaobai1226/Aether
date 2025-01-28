package com.xiaobai1226.aether.domain.vo.common;

import lombok.Data;

/**
 * 分页实体类VO
 *
 * @author bai
 */
@Data
public class PageVO {
    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页元素数
     */
    private Integer pageSize;

    /**
     * 排序字段
     */
    private Integer sortField;

    /**
     * 排序顺序 1 升序 2 降序
     */
    private Integer sortOrder;
}