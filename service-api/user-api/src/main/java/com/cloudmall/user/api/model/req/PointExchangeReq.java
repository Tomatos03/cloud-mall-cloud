package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分兑换请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointExchangeReq {

    @NotNull(message = "兑换商品ID不能为空")
    private Long itemId;

    private String idempotencyKey;
}
