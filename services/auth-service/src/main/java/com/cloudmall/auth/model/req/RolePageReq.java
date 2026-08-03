package com.cloudmall.auth.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.auth.entity.RoleDO;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 角色分页查询请求
 * <p>
 * 提供分页参数及角色筛选条件。
 * 查询条件由服务层根据 {@link #getName()} / {@link #getEnabled()} 手动构建。
 *
 * @author : Tomatos
 * @date : 2026/8/7
 */
@Getter
@Setter
public class RolePageReq extends PageReq<RoleDO> {

    /** 角色名称（模糊查询） */
    private String name;

    /** 是否启用 */
    private Boolean enabled;

    /**
     * 使用指定参数构造对象。
     */
    public RolePageReq() {}

    /**
     * 兼容 URL 参数 ?page=&size=
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public RolePageReq(long current, long size) {
        super(current, size);
    }
}
