package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_goods_spec")
public class GoodsSpecDO {
    private Long id;
    private Long goodsId;
    private String name;
    private String value;
    private Integer sortOrder;
}