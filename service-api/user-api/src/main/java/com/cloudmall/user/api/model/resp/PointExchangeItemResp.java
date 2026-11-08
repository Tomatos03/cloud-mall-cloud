package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分兑换商品。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointExchangeItemResp {

    private Long id;

    private String name;

    private String type;

    private Integer pointsCost;

    private Long couponId;

    private Integer stock;

    /** 当前用户是否已兑换过 */
    private Boolean exchanged;
}
