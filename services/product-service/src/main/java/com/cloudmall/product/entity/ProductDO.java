package com.cloudmall.product.entity;

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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_product")
public class ProductDO {
    private Long id;
    private String name;
    private String spuCode;
    private String description;
    private Long categoryId;
    private Long brandId;
    private Long imageId;
    private Long shopId;

    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLED;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
