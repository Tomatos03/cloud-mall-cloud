package com.cloudmall.user.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源创建/更新请求
 *
 * @author : Tomatos
 * @date : 2026/8/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceReq {

    /** 资源类型（menu / catalog / layout / button） */
    private String type;

    /** 权限编码（如 "product:create"），按钮类型必填 */
    private String code;

    /** 资源名称（显示名） */
    private String name;

    /** 父资源 ID（构建菜单树） */
    private Long parentId;

    /** 排序号 */
    private Integer sort;

    /** 是否启用 */
    private Boolean enabled;
}
