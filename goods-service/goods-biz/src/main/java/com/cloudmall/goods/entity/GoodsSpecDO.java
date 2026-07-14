package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cm_goods_spec")
public class GoodsSpecDO {
    private Long id;
    private Long goodsId;
    private String name;
    private String value;
    private Integer sortOrder;
}
