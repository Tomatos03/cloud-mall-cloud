package com.cloudmall.product.api.request;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 品牌更新请求。
 *
 * @author : Tomatos
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BrandUpdateReq {

    private String name;
    private String nameEn;
    private String logo;
    private String description;
    private String country;
    private String website;
    private Integer sortOrder;
    private StatusEnum status;
}
