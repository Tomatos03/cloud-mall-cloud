package com.cloudmall.product.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_product_sku")
public class ProductSkuDO {
    private Long id;

    @TableField("product_id")
    private Long productId;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer stock;
    private BigDecimal weight;
    private String image;

    /**
     * 规格值 ID 升序拼接的组合指纹（如 {@code "101_201"}），用于同一 SPU 下规格组合防重。
     * <p>
     * 为什么需要 hash 字段：
     * SKU 与规格值是多对多关系，"某个 SKU 的规格组合"是关联表（cm_product_sku_spec）中的多行记录，
     * 数据库无法直接对一组行做唯一性约束。spec_hash 把一组规格值 ID 压缩成单列字符串，
     * 使"组合唯一性"退化为"单列唯一性"，可被数据库原生约束 uk_product_spec (product_id, spec_hash) 直接拦截。
     * <p>
     * 为什么升序拼接：规格组合是无序集合，"红+XL"与"XL+红"是同一组合。
     * 升序排序保证同一组合无论传入顺序如何都得到相同指纹，唯一性判断与顺序无关。
     * <p>
     * 为什么依赖数据库层而非应用层：validateSkuTopology 的 seen 校验是"先查再插"(check-then-act)，
     * 高并发下两个请求可能同时通过校验、同时插入，应用层挡不住。
     * 唯一索引 uk_product_spec 是数据库原子性的硬约束，第二个插入会直接报唯一键冲突，
     * 是唯一能挡住并发重复提交的手段。
     */
    private String specHash;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
