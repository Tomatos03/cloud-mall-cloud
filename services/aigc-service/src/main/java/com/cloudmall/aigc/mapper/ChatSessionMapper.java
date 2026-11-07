package com.cloudmall.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.aigc.model.entity.ChatSessionDO;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {

    /**
     * 分页查询用户最近会话（按更新时间倒序）。 <p>
     * 条件：用户 ID 匹配，按更新时间倒序。
     *
     * @param page 分页参数
     * @param userId 用户 ID
     * @return 用户会话分页数据
     */
    default IPage<ChatSessionDO> selectRecentByUser(Page<ChatSessionDO> page, Long userId) {
        return selectPage(
                page,
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getUserId, userId)
                        .orderByDesc(ChatSessionDO::getUpdateTime)
        );
    }

    /**
     * 查询指定用户的会话。 <p>
     * 条件：会话 ID 匹配且属于该用户。
     *
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 会话，不存在时返回 null
     */
    default ChatSessionDO selectBySessionIdAndUserId(String sessionId, Long userId) {
        return selectOne(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .eq(ChatSessionDO::getSessionId, sessionId)
                        .eq(ChatSessionDO::getUserId, userId)
        );
    }
}
