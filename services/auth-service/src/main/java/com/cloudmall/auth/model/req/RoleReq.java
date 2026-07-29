package com.cloudmall.auth.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色创建/更新请求
 * <p>
 * 创建时所有字段必填，更新时仅传入需要变更的字段即可，未传的字段保持不变。
 *
 * @author : Tomatos
 * @date : 2026/8/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleReq {

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;

    /** 是否启用（更新时 null 表示不更新） */
    private Boolean enabled;
}
