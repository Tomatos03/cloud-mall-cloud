package com.cloudmall.product.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品牌创建请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCreateReq {
    private String name;
    private String nameEn;
    private String logo;
    private String description;
    private String country;
    private String website;
    private Integer sort;
}
