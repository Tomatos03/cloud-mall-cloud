package com.cloudmall.auth.entity;

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
 * 角色实体
 * <p>
 * 对应数据库表 cm_role，记录系统角色定义。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_role")
public class RoleDO {

    @TableId
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;

    /** 是否启用 */
    private Boolean enabled;

    /** 是否为内置角色（不可删除） */
    @Builder.Default
    private Boolean builtIn = Boolean.FALSE;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
