package com.cloudmall.common.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应契约。
 * <p>
 * 只暴露分页查询所需的业务字段，不依赖任何分页框架实现类型。
 *
 * @param <T> 列表元素类型
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResp<T> {

    /** 当前页数据 */
    private List<T> records;

    /** 总条数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /**
     * 构建空分页响应。
     *
     * @param current 当前页码
     * @param size 每页条数
     * @param <T> 列表元素类型
     * @return 空分页响应
     */
    public static <T> PageResp<T> empty(long current, long size) {
        return PageResp.<T>builder()
                       .records(List.of())
                       .current(current)
                       .size(size)
                       .build();
    }
}
