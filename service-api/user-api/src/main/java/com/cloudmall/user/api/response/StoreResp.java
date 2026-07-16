package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class StoreResp {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeLogo;
    private String description;
    private Integer status;
}
