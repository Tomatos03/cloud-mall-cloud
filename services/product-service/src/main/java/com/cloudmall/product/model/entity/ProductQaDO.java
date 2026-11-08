package com.cloudmall.product.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.mybatisplus.enums.StatusEnum;

/**
 * 商品问答实体。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_product_qa")
public class ProductQaDO {

    private Long id;

    private Long productId;

    private String question;

    private String answer;

    private Integer helpfulCount;

    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLED;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
