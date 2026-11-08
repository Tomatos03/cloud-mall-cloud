package com.cloudmall.im.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.im.model.entity.ChatSessionDO;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 会话分页查询请求
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Getter
@Setter
public class SessionPageReq extends PageReq<ChatSessionDO> {

    /**
     * 使用默认分页参数构造对象，供 Spring MVC 绑定查询参数。
     */
    public SessionPageReq() {
        super();
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 每页条数
     */
    public SessionPageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 兼容 URL 参数 ?page=。
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 兼容 URL 参数 ?pageSize=。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        setSize(pageSize);
    }
}
