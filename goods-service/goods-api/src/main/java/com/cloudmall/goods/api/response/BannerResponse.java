package com.cloudmall.goods.api.response;

import lombok.Data;

@Data
public class BannerResponse {
    private Long id;
    private String title;
    private String image;
    private String linkUrl;
    private Integer sortOrder;
}
