package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;

/**
 * 回收站实体类VO
 * TODO 目前怎是没有使用
 *
 * @author bai
 */
@Data
public class RecycleBinVO {
    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页元素数
     */
    private Integer pageSize;
}