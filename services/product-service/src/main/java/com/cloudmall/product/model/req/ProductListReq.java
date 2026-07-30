package com.cloudmall.product.model.req;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.ConditionReq;

/**
 * 商品列表查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 * 适用于非分页的商品列表/搜索场景。
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

    /** 品牌（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private String brand;

    /** 最低价格 */
    @QueryCondition(property = "price", operator = Operator.GE)
    private BigDecimal minPrice;

    /** 最高价格 */
    @QueryCondition(property = "price", operator = Operator.LE)
    private BigDecimal maxPrice;

    /** 排序方式：price_asc, price_desc, sales, newest */
    private String sortBy;

    @Override
    public LambdaQueryWrapper<ProductDO> buildLambdaWrapper() {
        // 先处理 @QueryCondition 注解字段
        LambdaQueryWrapper<ProductDO> wrapper = super.buildLambdaWrapper();

        // 排序逻辑（注解无法表达的条件，手动补充）
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc"  -> wrapper.orderByAsc(ProductDO::getPrice);
                case "price_desc" -> wrapper.orderByDesc(ProductDO::getPrice);
                case "sales"      -> wrapper.orderByDesc(ProductDO::getSalesCount);
                default           -> wrapper.orderByDesc(ProductDO::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(ProductDO::getCreateTime);
        }
        return wrapper;
    }
}
