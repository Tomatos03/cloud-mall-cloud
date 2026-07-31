package com.cloudmall.product.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单位创建请求。
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UnitCreateReq {

    private String name;
    private Integer sortOrder;
}
