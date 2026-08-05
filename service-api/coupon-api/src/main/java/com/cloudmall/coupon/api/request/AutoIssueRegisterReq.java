package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册自动发券请求
 * <p>
 * 内部接口，由消费服务消费 auth-service 注册事件后调用，按事件 ID 幂等发放新人券。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoIssueRegisterReq {

    /** 用户 ID */
    @NotNull
    private Long userId;

    /** 注册事件 ID（幂等键） */
    @NotBlank
    private String eventId;
}
