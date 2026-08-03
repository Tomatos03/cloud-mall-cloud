package com.cloudmall.seckill.api.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateReq {

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createUser;
}
