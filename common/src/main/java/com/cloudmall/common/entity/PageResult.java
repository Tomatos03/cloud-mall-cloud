package com.cloudmall.common.entity;

import java.util.Collections;
import java.util.List;

import lombok.Data;

/**
 * 分页结果封装
 *
 * @author Tomatos
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long pageSize;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long page, long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, page, pageSize);
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }
}