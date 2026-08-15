package com.cloudmall.user.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色响应对象
 * <p>
 * 作为业务层数据载体，不绑定任何 ORM 框架。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResp {

    /** 角色 ID */
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;

    /** 是否启用 */
    private boolean enabled;

    /** 是否内置角色（不可删除） */
    private boolean builtIn;

    /** 创建时间 */
    private LocalDateTime createTime;
}
