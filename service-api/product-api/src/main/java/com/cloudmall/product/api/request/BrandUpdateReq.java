package com.cloudmall.product.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.mybatisplus.enums.StatusEnum;

/**
 * 品牌更新请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateReq {

    private String name;
    private String nameEn;
    private String logo;
    private String description;
    private String country;
    private String website;
    private Integer sort;
    private StatusEnum status;
}
