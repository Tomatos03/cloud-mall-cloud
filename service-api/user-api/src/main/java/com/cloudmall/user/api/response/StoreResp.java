package com.cloudmall.user.api.response;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StoreResp {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeLogo;
    private String description;
    private StatusEnum status;
}