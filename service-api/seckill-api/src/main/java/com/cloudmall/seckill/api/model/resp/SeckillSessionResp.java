package com.cloudmall.seckill.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀场次响应
 * <p>
 * 用户端秒杀中心固定返回当天 00:00 - 23:00 共 24 个场次，没有活动的场次
 * {@link #hasActivity} 为 false 且状态为 {@code NOT_OPENED}。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillSessionResp {

    /** 场次整点（0 - 23） */
    private Integer hour;

    /** 场次展示时间（HH:00） */
    private String time;

    /** 当前整点是否有对应活动 */
    private Boolean hasActivity;

    /** 活动 ID（无活动时为 null） */
    private Long activityId;

    /** 活动名称（无活动时为 null） */
    private String activityName;

    /** 场次开始时间（无活动时为当天该整点） */
    private LocalDateTime startTime;

    /** 场次结束时间（无活动时为当天该整点后 1 小时） */
    private LocalDateTime endTime;

    /** 场次状态（NOT_OPENED/UPCOMING/ONGOING/ENDED） */
    private String status;
}
