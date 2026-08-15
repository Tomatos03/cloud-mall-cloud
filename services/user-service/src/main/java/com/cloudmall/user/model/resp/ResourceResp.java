package com.cloudmall.user.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源响应对象
 * <p>
 * 作为业务层数据载体。
 * Resource 可表示菜单、目录、布局或按钮，通过 type 区分。
 * code 字段作为权限标识时格式为 "business:action"（如 "goods:create"）。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResp {

    /** 资源 ID */
    private Long id;

    /** 资源类型（menu / catalog / layout / button） */
    private String type;

    /** 权限编码（如 "goods:create"），按钮类型必填 */
    private String code;

    /** 资源名称（显示名） */
    private String name;

    /** 父资源 ID（构建菜单树） */
    private Long parentId;

    /** 排序号 */
    private Integer sort;

    /** 是否启用 */
    private Boolean enabled;

    /** 创建时间 */
    private LocalDateTime createTime;
}
