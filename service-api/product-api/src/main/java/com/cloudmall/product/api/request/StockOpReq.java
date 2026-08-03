package com.cloudmall.product.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占/释放请求
 * <p>
 * 内部接口，由订单服务调用。同一订单号同一 SKU 同一操作类型幂等，
 * 重复调用不重复修改库存。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOpReq {

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;

    /** 库存操作明细 */
    @NotEmpty
    private List<@Valid StockReserveItem> items;

    /** 操作原因（可空） */
    private String reason;
}
