package com.cloudmall.im.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.Setter;

import com.cloudmall.im.entity.ChatSessionDO;
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

    /**
     * 构建查询条件：按创建时间倒序。
     *
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<ChatSessionDO> buildLambdaWrapper() {
        LambdaQueryWrapper<ChatSessionDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(ChatSessionDO::getCreateTime);
        return wrapper;
    }
}
