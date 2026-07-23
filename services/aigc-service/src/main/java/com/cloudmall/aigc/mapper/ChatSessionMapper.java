package com.cloudmall.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.aigc.entity.ChatSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {
}
