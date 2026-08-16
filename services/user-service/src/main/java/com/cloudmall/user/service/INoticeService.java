package com.cloudmall.user.service;

import java.util.List;

import com.cloudmall.user.model.resp.NoticeResp;

/**
 * 首页公告服务
 *
 * @author : Tomatos
 */
public interface INoticeService {

    /**
     * 查询启用中的公告（按排序值升序）。
     *
     * @return 公告响应列表
     */
    List<NoticeResp> listEnabled();
}
