package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.user.entity.NoticeDO;
import com.cloudmall.user.model.resp.NoticeResp;

/**
 * 首页公告转换器
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface NoticeConverter {

    /**
     * 转换对象数据。
     *
     * @param notice 公告实体
     * @return 处理结果
     */
    NoticeResp toResp(NoticeDO notice);

    /**
     * 转换对象数据。
     *
     * @param notices 公告实体列表
     * @return 数据列表
     */
    List<NoticeResp> toRespList(List<NoticeDO> notices);
}
