package com.cloudmall.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShopResp {
    private Long id;
    private Long userId;
    private String name;
    private String logo;
    private String description;
}
