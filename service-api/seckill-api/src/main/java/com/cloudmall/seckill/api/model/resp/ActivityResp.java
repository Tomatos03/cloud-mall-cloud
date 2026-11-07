package com.cloudmall.seckill.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀活动响应（列表视图）
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResp {

    /** 活动 ID */
    private Long id;

    /** 活动名称 */
    private String name;

    /** 活动描述 */
    private String description;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 每活动最大商品数 */
    private Integer maxProducts;

    /** 状态 */
    private String status;
}
