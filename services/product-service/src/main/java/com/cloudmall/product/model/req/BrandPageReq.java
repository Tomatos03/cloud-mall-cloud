package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.product.model.entity.BrandDO;

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
public class BrandPageReq extends PageReq<BrandDO> {

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

    /**
     * 构建所需对象。
     *
     * @return 处理结果
     */
    @Override
    public LambdaQueryWrapper<BrandDO> buildLambdaWrapper() {
        LambdaQueryWrapper<BrandDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByAsc(BrandDO::getSort);
        return wrapper;
    }
}
