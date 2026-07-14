package com.cloudmall.im.model.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页响应视图对象
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageVO<T> {
    /**
     * 数据记录列表
     */
    @Builder.Default
    private List<T> records = new ArrayList<>();

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;

    /**
     * 计算并设置 hasMore 字段
     */
    public void calculateHasMore() {
        if (this.total != null && this.page != null && this.pageSize != null) {
            this.hasMore = (long) this.page * this.pageSize < this.total;
        }
    }
}
