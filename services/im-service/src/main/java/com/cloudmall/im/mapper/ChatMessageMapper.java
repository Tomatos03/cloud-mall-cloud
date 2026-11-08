package com.cloudmall.im.mapper;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.im.enums.ReadStatusEnum;
import com.cloudmall.im.model.entity.ChatMessageDO;
import com.cloudmall.im.model.req.MessageCursorReq;
import com.cloudmall.mybatisplus.enums.LimitEnum;

/**
 * 聊天消息 Mapper
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDO> {

    /**
     * 游标分页获取会话历史消息（ID 游标，倒序加载，第一页为最新消息）。 <p>
     * 游标为上一页最后一条（最旧一条）消息的 ID，首次查询传 0 表示从最新开始； 多取一条用于判断是否
     * 还有更早的消息。
     *
     * @param req 游标查询请求
     * @return 消息游标分页数据
     */
    default CursorResult<ChatMessageDO> cursorHistory(MessageCursorReq req) {
        int size = Math.max(1, Math.min(ObjectUtil.defaultIfNull(req.getSize(), 10), CursorReq.MAX_SIZE));
        LambdaQueryWrapper<ChatMessageDO> wrapper = Wrappers.<ChatMessageDO>lambdaQuery()
                .eq(ChatMessageDO::getSessionId, req.getSessionId());
        if (req.getCursor() != null && req.getCursor() > 0) {
            wrapper.lt(ChatMessageDO::getId, req.getCursor());
        }
        wrapper.orderByDesc(ChatMessageDO::getId);
        wrapper.last(LimitEnum.of(size + 1));

        List<ChatMessageDO> records = selectList(wrapper);
        boolean hasMore = records.size() > size;
        List<ChatMessageDO> items = hasMore ? records.subList(0, size) : records;
        Long nextCursor = CollUtil.isEmpty(items) ? 0L : CollUtil.getLast(items).getId();
        return CursorResult.<ChatMessageDO>builder()
                           .items(items)
                           .cursor(nextCursor)
                           .hasMore(hasMore)
                           .build();
    }

    /**
     * 获取会话最后一条消息。
     *
     * @param sessionId 会话ID
     * @return 最后一条消息，无消息时返回 null
     */
    default ChatMessageDO selectLastMessage(Long sessionId) {
        IPage<ChatMessageDO> page = selectPage(
                new Page<>(1, 1),
                Wrappers.<ChatMessageDO>lambdaQuery()
                        .eq(ChatMessageDO::getSessionId, sessionId)
                        .orderByDesc(ChatMessageDO::getCreateTime)
        );
        return CollUtil.getFirst(page.getRecords());
    }

    /**
     * 统计会话未读消息数（排除自己发送的）。
     *
     * @param sessionId 会话ID
     * @param currentUserId 当前用户ID
     * @return 未读消息数
     */
    default Long countUnread(Long sessionId, Long currentUserId) {
        return selectCount(
                Wrappers.<ChatMessageDO>lambdaQuery()
                        .eq(ChatMessageDO::getSessionId, sessionId)
                        .eq(ChatMessageDO::getIsRead, ReadStatusEnum.UNREAD.getValue())
                        .ne(ChatMessageDO::getSenderId, currentUserId)
        );
    }

    /**
     * 将会话中他人发送的消息标记为已读。
     *
     * @param sessionId 会话ID
     * @param excludeSenderId 排除的发送者（当前用户自己）
     * @return 更新的记录数
     */
    default int markRead(Long sessionId, Long excludeSenderId) {
        return update(
                Wrappers.<ChatMessageDO>lambdaUpdate()
                        .eq(ChatMessageDO::getSessionId, sessionId)
                        .ne(ChatMessageDO::getSenderId, excludeSenderId)
                        .set(ChatMessageDO::getIsRead, ReadStatusEnum.READ.getValue())
        );
    }
}
