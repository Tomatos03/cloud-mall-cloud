package com.cloudmall.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源权限实体
 * <p>
 * 对应数据库表 cm_resource，存储系统中所有可授予的权限资源。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_resource")
public class ResourceDO {

    /**
     * 资源 ID
     */
    @TableId
    private Long id;

    /**
     * 权限编码（如 product:view, order:create）
     */
    private String code;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源类型（api / menu / button）
     */
    private String type;

    /**
     * 父资源 ID
     */
    private Long parentId;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 软删除标记
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
