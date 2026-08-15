package com.cloudmall.user.model.resp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源树节点
 * <p>
 * 包含资源基本信息及其子节点列表，用于构建前端菜单树。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceNodeResp {

    /** 资源信息 */
    @JsonUnwrapped
    private ResourceResp resource;

    /** 子节点列表（叶子节点为空列表） */
    private List<ResourceNodeResp> children;
}
