package com.cloudmall.common.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游标分页结果
 *
 * @author : Tomatos
 * @param <T> 列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorResult<T> {

    /** 当前页数据 */
    private List<T> items;

    /** 下一页游标（上一页最后一条记录的 ID），为 0 表示无更多数据 */
    private Long cursor;

    /** 是否还有更多数据 */
    private Boolean hasMore;
}
