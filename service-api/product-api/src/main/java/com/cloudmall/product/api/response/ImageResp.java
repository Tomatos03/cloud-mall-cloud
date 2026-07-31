package com.cloudmall.product.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品图片响应对象
 * <p>
 * 封装商品图片信息，包含图片地址、是否主图及排序。
 *
 * @author : Tomatos
 * @date : 2026/9/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResp {

    /** 图片 ID */
    private Long imageId;

    /** 图片 URL */
    private String url;

    /** 是否为主图 */
    private Boolean isMain;

    /** 排序号 */
    private Integer sortOrder;
}
