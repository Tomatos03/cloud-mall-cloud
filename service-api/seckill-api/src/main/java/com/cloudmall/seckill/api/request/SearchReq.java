package com.cloudmall.seckill.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchReq {
    private String status;
    private Integer page = 1;
    private Integer pageSize = 20;
}
