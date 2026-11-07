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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_comment")
public class CommentDO {
    private Long id;
    @TableField("product_id")
    private Long productId;
    private Long userId;
    private Long orderId;
    private String content;
    private Integer rating;
    private String images;
    private Integer usefulCount;
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
