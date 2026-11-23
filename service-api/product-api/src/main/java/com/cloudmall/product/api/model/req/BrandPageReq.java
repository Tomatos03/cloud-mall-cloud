package com.cloudmall.product.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.enums.StatusEnum;
import com.cloudmall.common.model.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 品牌分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandPageReq extends PageReq {

    /** 状态（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 品牌名称（模糊匹配） */
    @QueryCondition(operator = OperatorEnum.LIKE)
    private String name;

    /** 品牌英文名称（模糊匹配） */
    @QueryCondition(operator = OperatorEnum.LIKE)
    private String nameEn;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public BrandPageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 兼容 URL 参数 ?page=。
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 兼容 URL 参数 ?pageSize=。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        setSize(pageSize);
    }

}
