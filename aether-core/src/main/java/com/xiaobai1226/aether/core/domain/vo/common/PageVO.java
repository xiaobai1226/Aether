package com.xiaobai1226.aether.core.domain.vo.common;

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
}