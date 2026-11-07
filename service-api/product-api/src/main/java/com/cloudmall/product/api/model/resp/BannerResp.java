package com.cloudmall.product.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResp {
    private Long id;
    private String title;
    private String image;
    private String linkUrl;
    private Integer sort;
}
