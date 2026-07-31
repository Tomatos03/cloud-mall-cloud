package com.cloudmall.product.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值响应对象
 * <p>
 * 封装规格的单个可选值，包含值名称及关联图片。
 *
 * @author : Tomatos
 * @date : 2026/9/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecValueResp {

    /** 规格值 ID */
    private Long valueId;

    /** 规格值名称 */
    private String value;

    /** 规格值关联图片 URL */
    private String image;
}
