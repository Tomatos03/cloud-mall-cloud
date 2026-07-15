package com.cloudmall.order.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResp {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private List<ItemResp> items;
}
