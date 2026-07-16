package com.cloudmall.seckill.api.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchReq {
    private String status;
    private Integer page = 1;
    private Integer pageSize = 20;
}