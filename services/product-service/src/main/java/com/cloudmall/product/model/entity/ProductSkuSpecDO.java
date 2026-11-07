package com.cloudmall.product.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_product_sku_spec")
public class ProductSkuSpecDO {
    private Long skuId;
    private Long specValueId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
