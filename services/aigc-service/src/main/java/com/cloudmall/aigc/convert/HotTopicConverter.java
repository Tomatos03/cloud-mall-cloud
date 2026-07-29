package com.cloudmall.aigc.convert;

import org.mapstruct.Mapper;

import com.cloudmall.aigc.model.resp.HotTopicResp;
import com.cloudmall.aigc.properties.AgentProperties;

/**
 * 热门话题转换器
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface HotTopicConverter {

    /**
     * 将配置属性中的热门话题转换为响应对象。
     *
     * @param hotTopic 配置属性中的热门话题
     * @return 响应对象
     */
    HotTopicResp toResp(AgentProperties.HotTopic hotTopic);
}
