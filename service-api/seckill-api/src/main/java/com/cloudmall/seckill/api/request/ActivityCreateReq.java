package com.cloudmall.seckill.api.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀活动创建请求
 * <p>
 * 结束时间由服务端按"开始时间加1小时"推导，创建方无需传入。
 * 创建人取自登录态，不接受客户端传入。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateReq {

    /** 活动名称 */
    @NotBlank(message = "活动名称不能为空")
    private String name;

    /** 活动描述 */
    private String description;

    /** 开始时间（须晚于当前 24 小时，且必须为整点） */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 每活动最大商品数（1~500，不传默认 50） */
    private Integer maxProducts;
}
