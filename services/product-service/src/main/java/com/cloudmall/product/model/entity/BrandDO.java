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
@TableName("cm_brand")
public class BrandDO {
    private Long id;
    private String name;
    private String nameEn;
    private String logo;
    private String description;
    private String country;
    private String website;

    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLED;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
