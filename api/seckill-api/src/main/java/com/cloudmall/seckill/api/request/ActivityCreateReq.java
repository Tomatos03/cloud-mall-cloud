package com.cloudmall.seckill.api.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCreateReq {

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createUser;
}
