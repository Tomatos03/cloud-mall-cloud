package com.cloudmall.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行政区划。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_region")
public class RegionDO {

    @TableId
    private String code;

    private String parentCode;

    private String name;

    private Integer level;

    private Integer sort;

    private Boolean enabled;
}
