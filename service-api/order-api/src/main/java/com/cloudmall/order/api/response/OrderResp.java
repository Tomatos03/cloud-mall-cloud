package com.cloudmall.order.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResp {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createTime;
    private List<ItemResp> items;
}
