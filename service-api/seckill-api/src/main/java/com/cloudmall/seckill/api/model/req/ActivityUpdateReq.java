package com.cloudmall.seckill.api.model.req;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀活动编辑请求
 * <p>
 * DRAFT 状态可改全部字段；PUBLISHED 状态仅可改文案，时间字段被忽略
 * （需要调整时间时取消活动后重建）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateReq {

    /** 活动名称 */
    private String name;

    /** 活动描述 */
    private String description;

    /** 开始时间（仅 DRAFT 生效） */
    private LocalDateTime startTime;

    /** 每活动最大商品数（1~500，仅 DRAFT 生效，不传保持原值） */
    private Integer maxProducts;
}
