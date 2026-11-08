package com.cloudmall.user.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.user.convert.NoticeConverter;
import com.cloudmall.user.mapper.NoticeMapper;
import com.cloudmall.user.model.entity.NoticeDO;
import com.cloudmall.user.model.resp.NoticeResp;
import com.cloudmall.user.service.INoticeService;

/**
 * 首页公告服务实现
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class NoticeService implements INoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeConverter noticeConverter;

    /**
     * 查询启用中的公告（按排序值升序）。
     *
     * @return 公告响应列表
     */
    @Override
    public List<NoticeResp> listEnabled() {
        List<NoticeDO> notices = noticeMapper.selectEnabledOrdered();
        return noticeConverter.toRespList(notices);
    }
}
