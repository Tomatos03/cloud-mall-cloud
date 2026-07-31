package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.ConditionReq;
import com.cloudmall.product.entity.ProductDO;

/**
 * 商品列表查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 * 适用于非分页的商品列表/搜索场景。
 * <p>
 * 价格、品牌等跨表筛选由 Service 层在应用层组合完成，此处仅保留 SPU 级别的筛选条件。
 *
 * @author : Tomatos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ProductListReq extends ConditionReq<ProductDO> {

    /** 分类 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long categoryId;

    /** 状态（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 关键词（名称模糊匹配） */
    @QueryCondition(property = "name", operator = Operator.LIKE)
    private String keyword;

    /** 品牌 ID */
    @QueryCondition(operator = Operator.EQ)
    private Long brandId;

    /** 排序方式：newest */
    private String sortBy;

    /**
     * 构建所需对象。
     *
     * @return 处理结果
     */
    @Override
    public LambdaQueryWrapper<ProductDO> buildLambdaWrapper() {
        LambdaQueryWrapper<ProductDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(ProductDO::getCreateTime);
        return wrapper;
    }
}
