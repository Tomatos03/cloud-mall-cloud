package com.cloudmall.seckill.api.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeckillActivitySearchRequest {
    private String status;
    private Integer page = 1;
    private Integer pageSize = 20;
}
