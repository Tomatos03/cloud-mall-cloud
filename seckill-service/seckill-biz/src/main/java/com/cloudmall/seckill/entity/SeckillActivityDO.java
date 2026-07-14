package com.cloudmall.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cm_seckill_activity")
public class SeckillActivityDO {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
