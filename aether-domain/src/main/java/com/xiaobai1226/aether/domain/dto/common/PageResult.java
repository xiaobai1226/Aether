package com.xiaobai1226.aether.domain.dto.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页返回结果DTO类
 *
 * @author bai
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 分页对象构造函数
     *
     * @param page 分页对象
     */
    public static <T> PageResult<T> with(IPage<T> page) {
        return new PageResult<>(page);
    }

    /**
     * 分页对象构造函数
     *
     * @param page 分页对象
     */
    public PageResult(IPage<T> page) {
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.total = page.getTotal();
        this.totalPage = page.getPages();
        this.list = page.getRecords();
    }

    /**
     * 页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long totalPage;

    /**
     * 分页数据
     */
    private List<T> list;
}
