package com.cloudmall.product.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.product.enums.StockOpSourceEnum;
import com.cloudmall.product.enums.StockOpTypeEnum;

/**
 * 普通商品库存操作流水
 * <p>
 * 对应 cm_stock_op_log 表，记录库存预占、确认、释放和补偿操作。
 * 唯一索引 (order_no, sku_id, op_type) 保证同一订单同一 SKU 同一操作幂等。
 * 秒杀库存不接入本流水。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_stock_op_log")
public class StockOpLogDO {

    /** 库存操作记录 ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** SKU ID */
    private Long skuId;

    /** 商品 SPU ID */
    private Long productId;

    /** 店铺 ID */
    private Long shopId;

    /** 操作类型 */
    private StockOpTypeEnum opType;

    /** 操作数量（正数，方向由 opType 表达） */
    private Integer quantity;

    /** 操作前库存 */
    private Integer beforeStock;

    /** 操作后库存 */
    private Integer afterStock;

    /** 来源 */
    private StockOpSourceEnum source;

    /** 操作人 ID（系统操作为 NULL） */
    private Long operatorId;

    /** 原因 */
    private String reason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
