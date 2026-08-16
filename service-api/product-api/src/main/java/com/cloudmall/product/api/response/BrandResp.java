package com.cloudmall.product.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品牌响应对象。
 * <p>
 * 封装品牌完整信息，包含基础信息、状态及排序。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResp {

    private Long brandId;
    private String name;
    private String nameEn;
    private String logo;
    private String description;
    private String country;
    private String website;
    private Integer sort;
}
