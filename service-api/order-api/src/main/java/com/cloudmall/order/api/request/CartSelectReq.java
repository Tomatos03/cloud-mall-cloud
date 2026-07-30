package com.cloudmall.order.api.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选中/取消选中购物车商品请求
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartSelectReq {

    /** SKU ID 列表 */
    private Long skuId;

    /** 是否选中（true=选中，false=取消选中） */
    private Boolean selected;
}
