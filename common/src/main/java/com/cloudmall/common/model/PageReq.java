package com.cloudmall.common.model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询请求基类。
 * <p>
 * 只承载分页参数，不绑定任何 ORM 类型；分页对象由 service 层通过
 * {@code Pages.from(req)} 构建，查询条件由 {@code LambdaWrappers} 构建。
 *
 * @author : Tomatos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageReq {

    /** 当前页码，从 1 开始 */
    @Min(1)
    private long current = 1;

    /** 每页条数 */
    @Min(1)
    private long size = 10;

    /**
     * 兼容前端 page 参数。
     *
     * @param page 页码
     */
    public void setPage(long page) {
        this.current = page;
    }

    /**
     * 兼容前端 pageSize 参数。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(long pageSize) {
        this.size = pageSize;
    }
}
